import json
import pandas as pd
from kafka import KafkaConsumer, KafkaProducer
from sklearn.linear_model import LinearRegression
import numpy as np
import datetime
import sys

# Windowsコマンドプロンプトでの文字化け対策（UTF-8強制）
sys.stdout.reconfigure(encoding='utf-8')

# Kafka設定
KAFKA_TOPIC_INPUT = "scouter.score.input"
KAFKA_TOPIC_OUTPUT = "scouter.prediction.result"
KAFKA_BROKER = "localhost:9092"

# --- 予測ロジック本体 (predict_weekly_condition関数として定義) ---
def predict_weekly_condition(df_input):
    """過去のスコアデータを受け取り、来週の体調を予測する"""
    
    # 以前の予測ロジックをここに移植（中身は変更なし）
    if len(df_input) < 2: return []

    # ★修正箇所:
    # Java側で targetDate が 'YYYY-MM-DD' の文字列として送信されるようになったため、
    # 文字列の配列アクセス (x[0]-x[1]-x[2]) を削除し、to_datetimeで直接パースする
    df_input['date'] = df_input['targetDate'].apply(lambda x: pd.to_datetime(x))
    
    base_date = df_input['date'].min()
    df_input['days_passed'] = (df_input['date'] - base_date).dt.days

    X = df_input[['days_passed']]
    y = df_input['condition']
    
    model = LinearRegression()
    model.fit(X, y)

    last_date = df_input['date'].max()
    future_dates = [last_date + datetime.timedelta(days=i) for i in range(1, 8)]
    
    future_days_passed = [(d - base_date).days for d in future_dates]
    future_X = pd.DataFrame({'days_passed': future_days_passed})

    predictions = model.predict(future_X)
    
    final_results = []
    for date, score in zip(future_dates, predictions):
        clipped_score = min(max(round(score, 2), 1.0), 7.0)
        final_results.append({
            "date": date.strftime('%Y-%m-%d'), 
            "predicted_score": clipped_score 
        })
    
    return final_results


# --- 予測サービス実行関数 ---
def run_prediction_service():
    print(f"🚀 AI Engine Starting... Connecting to Kafka at {KAFKA_BROKER}")
    
    consumer = KafkaConsumer(
        KAFKA_TOPIC_INPUT,
        bootstrap_servers=KAFKA_BROKER,
        value_deserializer=lambda x: json.loads(x.decode('utf-8')),
        auto_offset_reset='latest',
        group_id='ai-engine-group'
    )
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BROKER,
        value_serializer=lambda x: json.dumps(x).encode('utf-8')
    )

    print("✅ Connected to Kafka! Waiting for messages...")

    for message in consumer:
        try:
            print(f"\n📩 Message Received! (Offset: {message.offset})")
            payload = message.value
            
            # 'history' キーからデータを取得する処理
            history_data = payload.get('history', []) 
            
            if not history_data or not isinstance(history_data, list):
                print("⚠️ Invalid or empty data received (Expected a list of scores under 'history').")
                continue

            # DataFrameに変換し、予測を実行
            df = pd.DataFrame(history_data)
            print("🧠 Starting Prediction Analysis...")
            prediction_results = predict_weekly_condition(df)

            if prediction_results:
                # 結果をDictでラップして返送 (messageIdを含める)
                response = {
                    "messageId": payload.get("messageId"),
                    "predictions": prediction_results 
                }
                
                producer.send(KAFKA_TOPIC_OUTPUT, value=response)
                producer.flush()
                print(f"📤 Prediction done! Sent {len(prediction_results)} results to topic '{KAFKA_TOPIC_OUTPUT}'")
            else:
                print("⚠️ Prediction skipped. No results sent.")

        except Exception as e:
            print(f"❌ Error processing message: {e}")

if __name__ == "__main__":
    run_prediction_service()