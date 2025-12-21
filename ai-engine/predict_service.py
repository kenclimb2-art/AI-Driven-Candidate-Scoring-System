import json
import os
import logging
import datetime
import sys
import pandas as pd
import numpy as np
from kafka import KafkaConsumer, KafkaProducer
from sklearn.linear_model import LinearRegression

# ロギング設定
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger(__name__)

# Windows対策
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')

# 設定の外部化
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "localhost:9092")
KAFKA_TOPIC_INPUT = os.getenv("KAFKA_TOPIC_INPUT", "scouter.score.input")
KAFKA_TOPIC_OUTPUT = os.getenv("KAFKA_TOPIC_OUTPUT", "scouter.prediction.result")
GROUP_ID = os.getenv("KAFKA_GROUP_ID", "ai-engine-group")

class PredictionEngine:
    """体調予測を行うAIエンジンクラス"""
    
    def calculate_scouter_score(self, df: pd.DataFrame):
        """
        Java側の『厳格査定ロジック』をPythonで再現。
        """
        # 1. 疲労以外の6項目の平均
        pos_cols = ['focus', 'efficiency', 'motivation', 'condition', 'sleepQuality', 'sexualDesire']
        base_avg = df[pos_cols].mean(axis=1)
        
        # 2. 疲労ペナルティの基本値 (0.0 〜 1.0)
        fatigue_penalty = (df['fatigue'] - 1) / 6.0
        
        # --- スーパーリビドーモード(7): オーバーヒート仕様 ---
        libido_mask = df['sexualDesire'] == 7
        base_avg.loc[libido_mask] = base_avg.loc[libido_mask] + 0.5 - 1.5
        fatigue_penalty.loc[libido_mask] = fatigue_penalty.loc[libido_mask] * 3.0
        
        # --- 賢者モード(1): 安定仕様 ---
        sage_mask = df['sexualDesire'] == 1
        fatigue_penalty.loc[sage_mask] = fatigue_penalty.loc[sage_mask] * 0.5
        
        # 最終スコア算出
        final_score = base_avg - fatigue_penalty
        return final_score.clip(1.0, 7.0)

    def predict_weekly_condition(self, df_input: pd.DataFrame):
        """過去のスコアデータを受け取り、将来7日間の推移を予測する"""
        if len(df_input) < 2:
            logger.warning("Insufficient data for prediction (need at least 2 points).")
            return []

        try:
            # Java側の新ロジックに基づいたターゲットスコアを算出
            # これにより、AIは「無理をしている高スコア」の後の失速を学習可能になる
            df_input['target_score'] = self.calculate_scouter_score(df_input)

            # 日付処理
            df_input['date'] = pd.to_datetime(df_input['targetDate'])
            base_date = df_input['date'].min()
            df_input['days_passed'] = (df_input['date'] - base_date).dt.days

            # 学習 (線形回帰)
            # 目的変数を 'condition' から 'target_score' に変更
            X = df_input[['days_passed']]
            y = df_input['target_score']
            
            model = LinearRegression()
            model.fit(X, y)

            # 予測 (将来7日間)
            last_date = df_input['date'].max()
            future_dates = [last_date + datetime.timedelta(days=i) for i in range(1, 8)]
            future_days_passed = pd.DataFrame({
                'days_passed': [(d - base_date).days for d in future_dates]
            })

            predictions = model.predict(future_days_passed)
            
            # 結果の整形
            return [
                {
                    "date": d.strftime('%Y-%m-%d'), 
                    "predicted_score": float(np.clip(round(s, 2), 1.0, 7.0)) 
                }
                for d, s in zip(future_dates, predictions)
            ]
        except Exception as e:
            logger.error(f"Prediction logic error: {e}")
            return []

def run_service():
    """Kafkaサービス実行メインループ"""
    engine = PredictionEngine()
    
    try:
        logger.info(f"🚀 AI Engine Starting... Broker: {KAFKA_BROKER}")
        
        consumer = KafkaConsumer(
            KAFKA_TOPIC_INPUT,
            bootstrap_servers=KAFKA_BROKER,
            value_deserializer=lambda x: json.loads(x.decode('utf-8')),
            auto_offset_reset='latest',
            group_id=GROUP_ID
        )
        
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BROKER,
            value_serializer=lambda x: json.dumps(x).encode('utf-8')
        )

        logger.info("✅ Connected to Kafka! Waiting for messages...")

        for message in consumer:
            try:
                payload = message.value
                message_id = payload.get("messageId", "unknown")
                logger.info(f"📩 Received request. ID: {message_id}")
                
                history_data = payload.get('history', []) 
                if not history_data:
                    logger.warning(f"Empty history in message {message_id}")
                    continue

                df = pd.DataFrame(history_data)
                
                # 予測実行
                prediction_results = engine.predict_weekly_condition(df)

                if prediction_results:
                    response = {
                        "messageId": message_id,
                        "predictions": prediction_results 
                    }
                    producer.send(KAFKA_TOPIC_OUTPUT, value=response)
                    producer.flush()
                    logger.info(f"📤 Sent prediction for ID: {message_id} ({len(prediction_results)} days)")

            except Exception as e:
                logger.error(f"Error processing message: {e}")

    except Exception as e:
        logger.critical(f"Failed to connect or run Kafka service: {e}")
        sys.exit(1)

if __name__ == "__main__":
    run_service()