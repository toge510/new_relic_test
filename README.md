# Weather アプリケーション

このリポジトリは、Apache HTTPD と Tomcat（Java Servlet）を使った簡単な天気アプリケーションです。
ブラウザから都市名を入力すると、OpenWeatherMap の外部 API を呼び出して現在の天気情報を表示します。

## 概要

- フロントは Apache HTTPD（リバースプロキシ）で受け、Tomcat の Servlet (`WeatherServlet`) にリクエストを中継します。
- Tomcat のサーブレットが OpenWeatherMap API を呼び出して JSON を返します。クライアント側（JSP + JavaScript）がその JSON を受け取り、見やすいカード形式で表示します。
- 使用する Tomcat イメージ: `tomcat:9.0.90-jdk8-temurin-jammy`（既に Dockerfile / docker-compose で指定されています）

## 事前準備

- Docker
- Docker Compose
- OpenWeatherMap の API キー（無料で取得可）: https://openweathermap.org/appid

注意: 現在 `docker-compose.yml` に環境変数 `WEATHER_API_KEY` が設定されていますが、セキュリティのため自分のキーに差し替えるか、Compose の環境設定を `.env` などで管理することを推奨します。

## 環境変数

- WEATHER_API_KEY: OpenWeatherMap の API キー（必須）

## 使い方（ローカル起動）

1. リポジトリのルートに移動:

```bash
cd /path/to/new_relic_test
```

2. 環境変数を設定（仮に bash を使う場合）:

```bash
export WEATHER_API_KEY=your_openweathermap_api_key_here
```

3. Docker Compose でビルド＆起動:

```bash
docker compose up --build
```

4. ブラウザでアクセス:

http://localhost:8000/

フォームに都市名（例: Tokyo）を入力して「Get Weather」を押すと、天気情報（気温、体感、湿度、風速、説明、アイコン）が表示されます。

## 動作確認・トラブルシューティング

- 表示が文字化けする場合は、ブラウザの Network タブで HTML レスポンスの `Content-Type` ヘッダに `charset=UTF-8` が含まれているか確認してください。
- サーバのログを確認するには、Compose のログを参照します:

```bash
docker compose logs -f
```

- コンテナ内から API にアクセスできない場合は、ホストやネットワーク設定（プロキシなど）を確認してください。

## 実装メモ（開発者向け）

- サーブレット: `tomcat/src/WeatherServlet.java`
	- OpenWeatherMap の REST エンドポイントを呼び出し、JSON をそのまま返しています。
	- 現在はレスポンスに `Content-Type: application/json; charset=UTF-8` を設定しています。
- フロント: `tomcat/webapp/index.jsp`
	- JavaScript(fetch) で `/weather?city=...` を呼び、受け取った JSON を DOM に描画します。

## MySQL の件について

元の要件で MySQL を含めることがありましたが、このリポジトリの現状の `docker-compose.yml` には MySQL サービスは含まれていません。もし天気データのキャッシュや履歴保存を行いたい場合は、`docker-compose.yml` に MySQL サービスを追加し、サーブレット側で JDBC 経由で保存/取得する実装を追加してください。

## 注意

- リポジトリ内の `docker-compose.yml` に API キーが平文で置かれている場合は、公開リポジトリにプッシュしないよう注意してください。`.env` を使うか CI/Secrets 機能で管理することを推奨します。

## ライセンス

このプロジェクトには特にライセンスファイルが含まれていません。使用・配布はリポジトリの所有者の方針に従ってください。

---
更新日: 2025-11-02
