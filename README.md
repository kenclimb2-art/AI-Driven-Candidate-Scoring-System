📚 Personal Scouter: 体調スコア予測システム
1. 💡 プロジェクト概要
本プロジェクトは、過去の体調スコアデータ（Daily Score）を基に、**AIモデル（Python/scikit-learn）**を用いて将来の体調を予測し、その結果をWebアプリケーション（Java/Spring Boot）にリアルタイムで表示するハイブリッド分散システムです。

JavaとPythonという異なる技術スタック間を、Apache Kafkaというメッセージングプラットフォームを介して非同期連携させることで、予測処理の分離と高いスケーラビリティを実現しました。

2. 🏗️ システムアーキテクチャ
システムは、以下の主要なコンポーネントで構成されるイベント駆動型アーキテクチャを採用しています。

コンポーネント	技術スタック	役割
フロントエンド/バックエンド	Java / Spring Boot / Thymeleaf	ユーザー入力、過去データの表示、予測依頼、予測結果の表示。
AI予測エンジン	Python / Kafka-python / scikit-learn	Kafkaからデータを受信し、線形回帰モデルで将来7日間のスコアを予測。
メッセージブローカー	Apache Kafka	JavaとPython間の非同期メッセージングを担うハブ。データパイプラインの中核。
データベース	H2 Database (or MySQL/PostgreSQL)	過去の体

3. 💾 連携のためのデータフロー
予測実行は以下の非同期なデータフローに従って動作します。

1.リクエスト送信 (Java → Kafka):
ユーザーがWeb画面で予測ボタンを押す。
BatchRunService がDBから全履歴データを取得し、JSON形式でKafkaの scouter.score.input トピックに送信。

2.予測処理 (Python):
Pythonのコンシューマが scouter.score.input からメッセージを受信。
データを使って線形回帰モデルを学習し、将来の予測スコアを生成。

3.結果送信 (Python → Kafka):
Pythonが予測結果リストをJSON形式のメッセージ（PredictionResponse）としてKafkaの scouter.prediction.result トピックに送信。

4.結果受信 (Java):
Javaの PredictionResultConsumer が scouter.prediction.result からメッセージを受信。
JSONを PredictionResponse オブジェクトに正しくデシリアライズし、メモリ（latestPredictions）に保持。

5.表示:
Web画面リロード時（または次の予測リクエスト完了後のリダイレクト時）に、Controllerが最新の予測結果を取得し、画面に表示。

4. ⚔️ 検証で得られた重要な知見 (技術的課題と解決)
今回のプロジェクト検証では、単なる機能実装を超え、非同期連携システム特有の以下の課題に直面し、解決しました。

課題	詳細	解決策
A. データ構造の不一致	Pythonが送信する予測結果のリスト構造が、JavaのPOJO (PredictionDataのリスト) にうまくデシリアライズできない。	Java側でリスト全体を包含するラッパーオブジェクト (PredictionResponse) を定義し、@KafkaListener の引数をラッパー型に変更することで解決。
B. 予測データの鮮度 (1サイクルラグ)	データを登録した直後に予測を実行しても、最新のデータが反映されない場合がある。	予測実行直前に短い時間 (Thread.sleep(100)) を設けることで、DBトランザクションのコミット遅延を回避し、最新データの取得を保証。 (※将来的にイベント駆動で解決推奨)
C. リアルタイム描画の安定性	SSEを導入しリアルタイム化を試みたが、ブラウザの切断による IOException が多発し、アプリケーションが不安定化。	一時的にSSEを削除し、安定性を優先した同期（リロードベース）の動作に戻す。 UI側でボタン連打防止のJS処理を加えることで、競合によるエラーを抑制。

5. 🚀 実行方法
（※ここでは環境構築手順は省略し、必要なコンポーネントのみ記載します。）

1.KafkaとPython環境の準備: Kafkaブローカーを起動し、PythonのAI予測エンジン（predict_service.py）を実行しておく。

2.Javaアプリケーションの起動: Spring BootアプリケーションをIDEまたはmvn spring-boot:runで起動。

3.Webアクセス: ブラウザで http://localhost:8081/ にアクセス。

4.予測実行: 画面上の「🚀 予測エンジン起動 (Kafka連携)」ボタンを押すと、非同期で予測が実行されます。

5.結果確認: 結果は非同期でJavaに戻され、画面をリロードすることで最新の結果が表示されます。
