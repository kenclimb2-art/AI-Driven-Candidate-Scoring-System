📚 Personal Scouter: 体調スコア予測システム
1. 💡 プロジェクト概要
本プロジェクトは、日々のパフォーマンスデータを基に、**AI（Python/scikit-learn）**を用いて将来の体調を予測し、その結果をWebアプリケーション（Java/Spring Boot）で可視化するハイブリッド分散システムです。
単なる平均計算ではなく、**「疲労による減衰」や「生命力の過剰燃焼（オーバーヒート）」**を考慮した独自の「スカウター・アルゴリズム」を搭載しており、持続可能な高パフォーマンス状態（黄金比：性欲5・疲労1）の維持を支援します。
2. 🏗️ システムアーキテクチャ
JavaとPythonという異なる技術スタック間を、Apache Kafkaを介して非同期連携させるイベント駆動型アーキテクチャを採用しています。
コンポーネント	技術スタック	役割
Backend	Java 17 / Spring Boot 3.5.x	ユーザー管理、実績データのUPSERT、Kafka連携、予測データの永続化
AI Engine	Python 3.12 / scikit-learn / Pandas	線形回帰モデルによる将来7日間のコンディション予測
Messaging	Apache Kafka	システム間の非同期メッセージング・ハブ
Database	PostgreSQL (Docker)	実績データ（DailyScore）および予測データ（PredictionScore）の永続化
Frontend	Thymeleaf / Bootstrap 5 / JS	スコア入力、ダッシュボード表示、非同期データの自動ポーリング更新
3. 🧠 スカウター・ロジック（厳格査定アルゴリズム）
本システムは、以下の独自ロジックに基づいて「真のコンディション」を算出します。
⚖️ ネット・パフォーマンス計算
疲労以外の6項目（集中、効率、意欲、体調、睡眠、性欲）の平均から、疲労度に応じたペナルティを差し引きます。
🔴 スーパーリビドーモード (性欲: 7)
短期的な出力は向上しますが、システムに過負荷がかかる「オーバークロック」状態です。
ブースト: ベーススコア微増
ペナルティ: 疲労の影響が 3倍 に増幅、さらに固定のシステム負荷が発生。
判定: 疲労が蓄積している場合、スコアは急落し「CRITICAL: OVERHEAT」警告が表示されます。
🔵 賢者モード (性欲: 1)
精神的安定と回復を優先した「安定稼働」状態です。
ボーナス: 疲労によるスコア減衰を 50%緩和。
4. 🔄 非同期データフロー & UX
Request: ユーザーが「予測エンジン起動」を押すと、JavaがDBをクリアしKafkaへ依頼を送信。
Processing: Pythonがメッセージを受信し、Javaと同じロジックで学習・予測を実行。
Persistence: JavaのConsumerが結果を受信し、PredictionScore テーブルに永続化。
Auto-Refresh: フロントエンドのJavaScriptがAPIをポーリングし、データ到着を検知すると自動で画面をリロードします。
5. 🚀 セットアップと実行
前提条件
Docker / Docker Compose
Java 17+
Python 3.12
手順
インフラの起動
code
Bash
docker-compose up -d
Pythonエンジンの起動
code
Bash
cd python-service
pip install -r requirements.txt
python predict_service.py
Javaアプリケーションの起動
code
Bash
./mvnw spring-boot:run
アクセス
http://localhost:8081
6. 🛠️ 技術的特徴（リファクタリングの成果）
型安全性の確保: Java Record (DTO) の導入と、厳格なNull安全アノテーションの適用。
データ整合性: 日付をキーとしたUPSERTロジックにより、重複データを排除。
クリーンコード: ドメイン駆動設計（DDD）の考え方を取り入れ、計算ロジックをEntityに集約。
スケーラビリティ: Kafkaによる疎結合な設計により、将来的なAIモデルの差し替えが容易。
💡 今後のロードマップ

予測精度向上のためのLSTMモデルへの移行

Chart.jsによるスコア推移の可視化グラフ

Spring Securityによるマルチユーザー対応