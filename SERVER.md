# 🌐 Rock-Paper-Scissors (SPS Game) - Server & Socket.io Guide

> **Language / Dil:** [🇬🇧 English](#-rock-paper-scissors-sps-game---server--socketio-guide) | [🇹🇷 Türkçe](#-taş-kağıt-makas-sps-game---sunucu--socketio-kurulum-kılavuzu)

---

# 🇬🇧 English Documentation

This document contains full details on local setup, deployment to [Render.com](https://render.com), Android client integration, and the real-time Socket.io API matrix for the **Rock-Paper-Scissors (SPS Game)** backend server.

---

## 📋 Table of Contents
1. [Architecture & Technologies](#1-architecture--technologies)
2. [Local Development & Execution](#2-local-development--execution)
3. [Render.com Deployment Guide](#3-rendercom-deployment-guide)
4. [Android Client Connection Config](#4-android-client-connection-config)
5. [Socket.io Event API](#5-socketio-event-api)
6. [Game Rules & Move Matrix](#6-game-rules--move-matrix)
7. [Troubleshooting & Tips](#7-troubleshooting--tips)

---

## 1. Architecture & Technologies

The server synchronizes player matchmaking, private rooms, move validations, consumable item rules, round outcomes, and cloud scores in real time.

- **Runtime:** Node.js (v18+)
- **Language:** TypeScript (~5.8)
- **Web Framework:** Express.js (v4.21)
- **Real-Time Layer:** Socket.io (v4.8)
- **Runner:** `tsx` (TypeScript Execute)

---

## 2. Local Development & Execution

### Prerequisites
- [Node.js](https://nodejs.org/) (v18 or higher) and `npm` installed on your machine.

### Step-by-Step Run:

1. **Navigate to the server directory:**
   ```bash
   cd server
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start in Development Mode (Live reload):**
   ```bash
   npm run dev
   ```
   *The server will start listening on `http://localhost:3000` by default.*

4. **Build the Project:**
   ```bash
   npm run build
   ```
   *Compiles TypeScript files into the `dist/` directory (`tsc`).*

5. **Start in Production Mode:**
   ```bash
   npm start
   ```

---

## 3. Render.com Deployment Guide

You can deploy the server as a free **Web Service** on [Render.com](https://render.com) for 24/7 global availability.

### Step 1: Prepare Repository
Push your project to a GitHub or GitLab repository.

### Step 2: Render.com Dashboard Setup
1. Log in to [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** and select **Web Service**.
3. Connect your GitHub/GitLab repository.
4. Enter the following configuration:

| Setting | Value / Description |
| :--- | :--- |
| **Name** | `rock-paper-scissors-android-game-server` *(or your chosen name)* |
| **Language / Runtime** | `Node` |
| **Root Directory** | `server` *(Important: Server code resides under `server/`)* |
| **Branch** | `main` *(or `master`)* |
| **Build Command** | `npm install && npm run build` |
| **Start Command** | `npm start` |
| **Instance Type** | `Free` |

### Step 3: Environment Variables
- Render.com automatically assigns the `PORT` variable (e.g. `10000` or `3000`). The server code uses `process.env.PORT || 3000`, so no manual environment variable configuration is required.

### Step 4: Deploy & Access Live URL
- Click **Create Web Service**.
- Once deployed, Render will provide a live HTTPS URL:
  `https://your-service-name.onrender.com`
- Visiting this URL in your browser should display `"SPS Game Socket.io Server is running."`.

---

## 4. Android Client Connection Config

Configure the server endpoint in `GameAppConfig.kt`:

- **File Path:** `app/src/main/java/com/onurkolofficial/spsgame/utils/GameAppConfig.kt`

```kotlin
package com.onurkolofficial.spsgame.utils

object GameAppConfig {
    // Start.io App ID
    const val STARTIO_APP_ID = "209638589"

    // Live Render.com Server Address
    const val SOCKET_URL = "https://rock-paper-scissors-android-game-server.onrender.com"

    // Local Testing (Android Emulator):
    // const val SOCKET_URL = "http://10.0.2.2:3000"

    // Local Testing (Physical Device on same Wi-Fi):
    // const val SOCKET_URL = "http://192.168.1.XX:3000"
}
```

> [!NOTE]
> With `https://`, Socket.io automatically establishes a secure WebSocket connection (`wss://`).

---

## 5. Socket.io Event API

### 📤 Client ➔ Server Events

| Event Name | Payload | Description |
| :--- | :--- | :--- |
| `request_player_count` | `{}` | Requests current online connected player count. |
| `join_matchmaking` | `{ name: string, skin?: string, mode?: "classic" \| "hard" }` | Enters matchmaking queue for Classic or Hard mode. |
| `create_private_room` | `{ name: string, skin?: string, allowSpecialItems?: boolean }` | Creates a custom private room with special items toggle. |
| `join_private_room` | `{ roomId: string, name: string, skin?: string }` | Joins an existing private room via Room ID code. |
| `send_move` | `{ move: "rock" \| "paper" \| "scissors" \| "iron" \| "ice" \| "steel" \| "fire" \| "lightning" \| "bomb" }` | Emits selected move for the active round. |
| `leave_game` | `{}` | Signals intentional forfeit / abandon. |
| `timeout_from_client` | `{}` | Triggered if the 10-second move timer expires. |

---

### 📥 Server ➔ Client Events

| Event Name | Payload | Description |
| :--- | :--- | :--- |
| `player_count` | `{ count: number }` | Active online player count update. |
| `waiting_for_opponent` | `{ roomId: string, mode?: string, allowSpecialItems?: boolean }` | Waiting in lobby for an opponent to join. |
| `private_room_created` | `{ roomId: string, allowSpecialItems?: boolean }` | Private room created with unique Room ID. |
| `join_error` | `{ message: string }` | Room full, not found, or invalid join error. |
| `match_found` | `{ roomId, round, draws, allowSpecialItems, mode, players: [...] }` | Match found with player info, mode, and item settings. |
| `game_starting` | `{}` | Countdown finished, start the game screen. |
| `opponent_moved` | `{}` | Informs player that opponent has made their move. |
| `round_result` | `{ result: "win" \| "lose" \| "draw", opponentMove, score, opponentScore, draws, round }` | Evaluated round outcome. |
| `next_round` | `{ round: number }` | Signals start of the next round. |
| `game_over` | `{ result: "win" \| "lose" \| "draw" }` | Final game outcome after 10 rounds. |
| `opponent_disconnected` | `{ wasPlaying: boolean, round: number }` | Opponent abandoned. (Awarded forfeit win if round >= 5). |

---

## 6. Game Rules & Move Matrix

Both server ([server.ts](file:///c:/Onur/Android%20Uygulamalar-Oyunlar/Rock-Paper-Scissors_Game/server/server.ts)) and client ([GameEngine.kt](file:///c:/Onur/Android%20Uygulamalar-Oyunlar/Rock-Paper-Scissors_Game/app/src/main/java/com/onurkolofficial/spsgame/model/GameEngine.kt)) share the identical 9-move battle matrix:

| Move | Wins Against | Loses To | Draws With |
| :--- | :--- | :--- | :--- |
| **Rock (Taş)** | Scissors, Iron, Fire, Lightning | Paper, Ice, Steel, Bomb | Rock |
| **Paper (Kağıt)** | Rock, Steel, Bomb, Lightning | Scissors, Ice, Iron, Fire | Paper |
| **Scissors (Makas)** | Paper | Rock, Iron, Ice, Steel, Fire, Lightning, Bomb | Scissors |
| **Iron (Demir)** | Rock, Scissors | Paper, Ice, Steel, Lightning, Bomb | Iron, Fire |
| **Ice (Buz)** | Rock, Paper, Scissors, Iron, Bomb | Steel, Fire, Lightning | Ice |
| **Steel (Çelik)** | Iron, Rock, Scissors, Ice, Fire | Paper, Lightning, Bomb | Steel |
| **Fire (Ateş)** | Paper, Ice, Scissors | Rock, Steel, Bomb, Lightning | Fire, Iron |
| **Lightning (Yıldırım)** | Steel, Iron, Scissors, Ice, Fire | Rock, Paper, Bomb | Lightning |
| **Bomb (Bomba)** | Rock, Iron, Steel, Scissors, Fire, Lightning | Paper, Ice | Bomb |

---

## 7. Troubleshooting & Tips

### 1. Render Free Tier Cold Start (Spin-down)
- On Render's free tier, services spin down after **15 minutes of inactivity**.
- Initial connection upon waking up may take **30-50 seconds**. Subsequent requests will respond instantly in milliseconds.
- *Tip:* Use free ping services like [UptimeRobot](https://uptimerobot.com/) or [Cron-Job.org](https://cron-job.org/) to send an HTTP GET request to `https://your-service.onrender.com/` every 10 minutes to prevent sleeping.

### 2. Android Cleartext Traffic (Local HTTP)
- Android 9+ blocks unencrypted `http://` network traffic by default. When testing locally with IP addresses, ensure `<application android:usesCleartextTraffic="true" ...>` is enabled in `AndroidManifest.xml`. (Not needed for production HTTPS/WSS).

---
---

# 🇹🇷 Türkçe Dokümantasyon

Bu doküman, **Taş-Kağıt-Makas (Rock-Paper-Scissors)** oyununun çevrim içi çok oyunculu (Online Multiplayer) modunu yöneten Node.js/TypeScript tabanlı Socket.io sunucusunun yerel kurulumu, Render.com üzerinde canlıya alınması, Android istemcisi ile entegrasyonu ve olay API matrisi hakkında tüm detayları içerir.

---

## 📋 İçindekiler
1. [Mimari ve Teknolojiler](#1-mimari-ve-teknolojiler-tr)
2. [Yerel Geliştirme ve Çalıştırma](#2-yerel-geliştirme-ve-çalıştırma-tr)
3. [Render.com Canlıya Alma (Deploy) Rehberi](#3-rendercom-canlıya-alma-deploy-rehberi-tr)
4. [Android İstemci Bağlantı Ayarı](#4-android-istemci-bağlantı-ayarı-tr)
5. [Socket.io Olayları (Event API)](#5-socketio-olayları-event-api-tr)
6. [Oyun Kuralları & Hamle Matrisi](#6-oyun-kuralları--hamle-matrisi-tr)
7. [Sıkça Karşılaşılan Sorunlar & İpuçları](#7-sıkça-karşılaşılan-sorunlar--ipuçları-tr)

---

## 1. Mimari ve Teknolojiler <a id="1-mimari-ve-teknolojiler-tr"></a>

Sunucu, oyuncuların eşleşmesini (matchmaking), özel oda kurmasını/katılmasını (private rooms), hamle doğrulamalarını, özel nesne kurallarını ve tur sonuçlarını gerçek zamanlı olarak senkronize eder.

- **Çalışma Ortamı:** Node.js (v18+)
- **Dil:** TypeScript (~5.8)
- **Web Çatısı:** Express.js (v4.21)
- **Gerçek Zamanlı İletişim:** Socket.io (v4.8)
- **Çalıştırıcı:** `tsx` (TypeScript Execute)

---

## 2. Yerel Geliştirme ve Çalıştırma <a id="2-yerel-geliştirme-ve-çalıştırma-tr"></a>

### Gereksinimler
- Bilgisayarınızda [Node.js](https://nodejs.org/) (sürüm 18 veya üzeri) ve `npm` kurulu olmalıdır.

### Adım Adım Çalıştırma:

1. **Sunucu klasörüne gidin:**
   ```bash
   cd server
   ```

2. **Bağımlılıkları yükleyin:**
   ```bash
   npm install
   ```

3. **Geliştirme Modunda Başlatın:**
   ```bash
   npm run dev
   ```
   *Sunucu varsayılan olarak `http://localhost:3000` portunda dinlemeye başlayacaktır.*

4. **Projeyi Derleyin (Build):**
   ```bash
   npm run build
   ```
   *TypeScript kodlarını `dist/` klasörüne derler (`tsc`).*

5. **Canlı/Prodüksiyon Modunda Başlatın:**
   ```bash
   npm start
   ```

---

## 3. Render.com Canlıya Alma (Deploy) Rehberi <a id="3-rendercom-canlıya-alma-deploy-rehberi-tr"></a>

[Render.com](https://render.com) üzerinde ücretsiz bir **Web Service** oluşturarak sunucunuzu 7/24 internete açık hale getirebilirsiniz.

### Adım 1: Depoyu Hazırlama
Projeyi bir GitHub/GitLab deposuna push edin.

### Adım 2: Render.com Dashboard Ayarları
1. [Render Dashboard](https://dashboard.render.com/)'a giriş yapın.
2. **New +** butonuna tıklayın ve **Web Service** seçeneğini seçin.
3. GitHub deponuzu bağlayın.
4. Aşağıdaki yapılandırma ayarlarını eksiksiz girin:

| Parametre | Değer / Açıklama |
| :--- | :--- |
| **Name** | `rock-paper-scissors-android-game-server` *(veya tercih ettiğiniz bir isim)* |
| **Language / Runtime** | `Node` |
| **Root Directory** | `server` *(Önemli: Sunucu kodları ana dizinde değil `server/` altındadır)* |
| **Branch** | `main` *(veya `master`)* |
| **Build Command** | `npm install && npm run build` |
| **Start Command** | `npm start` |
| **Instance Type** | `Free` |

### Adım 3: Ortam Değişkenleri (Environment Variables)
- Render.com `PORT` değişkenini otomatik olarak atar (örn: `10000` veya `3000`). Sunucu kodumuz `process.env.PORT || 3000` kullandığı için ekstra değişken tanımlamaya gerek yoktur.

### Adım 4: Deploy & Canlı URL Alma
- **Create Web Service** butonuna basın.
- Dağıtım tamamlandığında Render size özel bir alan adı verecektir:
  `https://your-service-name.onrender.com`
- Tarayıcınızdan bu adresi ziyaret ettiğinizde `"SPS Game Socket.io Server is running."` mesajını görüyorsanız sunucunuz sorunsuz çalışmaktadır.

---

## 4. Android İstemci Bağlantı Ayarı <a id="4-android-istemci-bağlantı-ayarı-tr"></a>

Android uygulamasının canlı sunucuya bağlanabilmesi için URL ayarı `GameAppConfig.kt` dosyasından yönetilir.

- **Dosya Yolu:** `app/src/main/java/com/onurkolofficial/spsgame/utils/GameAppConfig.kt`

```kotlin
package com.onurkolofficial.spsgame.utils

object GameAppConfig {
    // Start.io Reklam ID'si
    const val STARTIO_APP_ID = "209638589"

    // Canlı Render.com Sunucu Adresi
    const val SOCKET_URL = "https://rock-paper-scissors-android-game-server.onrender.com"

    // Yerel Test için (Android Emülatör):
    // const val SOCKET_URL = "http://10.0.2.2:3000"

    // Yerel Test için (Aynı Wi-Fi'daki Gerçek Cihaz):
    // const val SOCKET_URL = "http://192.168.1.XX:3000"
}
```

> [!NOTE]
> Render.com `https://` kullandığında Socket.io bağlantısı otomatik olarak güvenli WebSocket (`wss://`) protokolü üzerinden kurulur.

---

## 5. Socket.io Olayları (Event API) <a id="5-socketio-olayları-event-api-tr"></a>

### 📤 İstemciden Sunucuya Gönderilen Olaylar (Client ➔ Server)

| Olay Adı | Veri (Payload) | Açıklama |
| :--- | :--- | :--- |
| `request_player_count` | `{}` | Anlık çevrim içi oyuncu sayısını talep eder. |
| `join_matchmaking` | `{ name: string, skin?: string, mode?: "classic" \| "hard" }` | Seçilen modda (Klasik veya Zor) rastgele bir rakiple eşleşme sırasına girer. |
| `create_private_room` | `{ name: string, skin?: string, allowSpecialItems?: boolean }` | Özel oda oluşturur ("Nesneleri Kullan" ayarı ile). |
| `join_private_room` | `{ roomId: string, name: string, skin?: string }` | Kodunu bildiği özel odaya katılır. |
| `send_move` | `{ move: "rock" \| "paper" \| "scissors" \| "iron" \| "ice" \| "steel" \| "fire" \| "lightning" \| "bomb" }` | O tur için yapılan hamleyi iletir. (Klasik modda sadece taş-kağıt-makas geçerlidir). |
| `leave_game` | `{}` | Oyuncu maçtan veya odadan bilerek ayrıldığında (terk ettiğinde) sunucuya bildirir. |
| `timeout_from_client` | `{}` | Oyuncunun 10 saniyelik süresi dolduğunda otomatik turu sonlandırmak için çağrılır. |

---

### 📥 Sunucudan İstemciye Gönderilen Olaylar (Server ➔ Client)

| Olay Adı | Veri (Payload) | Açıklama |
| :--- | :--- | :--- |
| `player_count` | `{ count: number }` | Anlık bağlı aktif çevrim içi oyuncu sayısı. |
| `waiting_for_opponent` | `{ roomId: string, mode?: string, allowSpecialItems?: boolean }` | Eşleşme veya oda açıldıktan sonra rakip bekleniyor bildirimi. |
| `private_room_created` | `{ roomId: string, allowSpecialItems?: boolean }` | Özel oda başarıyla oluşturuldu, oda kodu üretildi. |
| `join_error` | `{ message: string }` | Oda dolu, bulunamadı veya katılım hatası bildirimi. |
| `match_found` | `{ roomId, round, draws, allowSpecialItems, mode, players: [...] }` | Rakip bulundu; oyuncu isimleri, mod ve nesne izinleri iletildi. |
| `game_starting` | `{}` | 3 saniyelik geri sayım sonrası maç ekranının yüklenmesi talimatı. |
| `opponent_moved` | `{}` | Rakibin hamlesini yaptığını bildirir (hamlenin içeriği tur bitene kadar gizlenir). |
| `round_result` | `{ result: "win" \| "lose" \| "draw", opponentMove, score, opponentScore, draws, round }` | Tur değerlendirme sonucu. |
| `next_round` | `{ round: number }` | Yeni turun başladığını bildirir. |
| `game_over` | `{ result: "win" \| "lose" \| "draw" }` | 10 tur tamamlandığında nihai maç galibini bildirir. |
| `opponent_disconnected` | `{ wasPlaying: boolean, round: number }` | Rakip maç devam ederken oyundan ayrıldı / terk etti. (5 turun üzerindeyse kalan oyuncu galip sayılır). |

---

## 6. Oyun Kuralları & Hamle Matrisi <a id="6-oyun-kuralları--hamle-matrisi-tr"></a>

Sunucu ([server.ts](file:///c:/Onur/Android%20Uygulamalar-Oyunlar/Rock-Paper-Scissors_Game/server/server.ts)) ile Android istemcisi ([GameEngine.kt](file:///c:/Onur/Android%20Uygulamalar-Oyunlar/Rock-Paper-Scissors_Game/app/src/main/java/com/onurkolofficial/spsgame/model/GameEngine.kt)) aynı kural matrisini işletir:

| Hamle | Kazanır (Yener) | Kaybeder (Yenilir) | Berabere |
| :--- | :--- | :--- | :--- |
| **Taş (Rock)** | Makas, Demir, Ateş, Yıldırım | Kağıt, Buz, Çelik, Bomba | Taş |
| **Kağıt (Paper)** | Taş, Çelik, Bomba, Yıldırım | Makas, Buz, Demir, Ateş | Kağıt |
| **Makas (Scissors)** | Kağıt | Taş, Demir, Buz, Çelik, Ateş, Yıldırım, Bomba | Makas |
| **Demir (Iron)** | Taş, Makas | Kağıt, Buz, Çelik, Yıldırım, Bomba | Demir, Ateş |
| **Buz (Ice)** | Taş, Kağıt, Makas, Demir, Bomba | Çelik, Ateş, Yıldırım | Buz |
| **Çelik (Steel)** | Demir, Taş, Makas, Buz, Ateş | Kağıt, Yıldırım, Bomba | Çelik |
| **Ateş (Fire)** | Kağıt, Buz, Makas | Taş, Çelik, Bomba, Yıldırım | Ateş, Demir |
| **Yıldırım (Lightning)** | Çelik, Demir, Makas, Buz, Ateş | Taş, Kağıt, Bomba | Yıldırım |
| **Bomba (Bomb)** | Taş, Demir, Çelik, Makas, Ateş, Yıldırım | Kağıt, Buz | Bomba |

---

## 7. Sıkça Karşılaşılan Sorunlar & İpuçları <a id="7-sıkça-karşılaşılan-sorunlar--ipuçları-tr"></a>

### 1. Render Free Tier Uyku Modu (Cold Start)
- Render.com'un ücretsiz planında sunucuya **15 dakika boyunca** herhangi bir istek gelmezse sunucu uyku moduna geçer (*spun down*).
- Bir kullanıcı uygulamayı açtığında ilk bağlantının kurulması **30-50 saniye** sürebilir. Sunucu uyandıktan sonra tüm istekler anlık (milisaniyeler içinde) yanıt verir.
- *İpucu:* Sunucuyu sürekli uyanık tutmak isterseniz [UptimeRobot](https://uptimerobot.com/) veya [Cron-Job.org](https://cron-job.org/) gibi ücretsiz servislerle sunucunuzun ana URL'sine (`https://your-service.onrender.com/`) her 10 dakikada bir HTTP GET isteği gönderebilirsiniz.

### 2. Yerel Testte Bağlantı Kurulamaması (Android Cleartext Traffic)
- Android 9 (Pie) ve üzerinde yerel IP'lere (`http://`) yapılan istekler varsayılan olarak engellenebilir. Yerel geliştirme yaparken `AndroidManifest.xml` dosyasında `<application android:usesCleartextTraffic="true" ...>` ayarının açık olduğundan emin olun (Canlı HTTPS/WSS bağlantılarında buna gerek yoktur).
