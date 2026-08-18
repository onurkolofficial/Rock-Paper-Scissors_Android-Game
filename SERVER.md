# 🌐 Rock-Paper-Scissors (SPS Game) - Sunucu & Socket.io Kurulum Kılavuzu

Bu doküman, **Taş-Kağıt-Makas (Rock-Paper-Scissors)** oyununun çevrim içi çok oyunculu (Online Multiplayer) modunu yöneten Node.js/TypeScript tabanlı Socket.io sunucusunun yerel kurulumu, Render.com üzerinde canlıya alınması ve Android istemcisi ile entegrasyonu hakkında tüm detayları içerir.

---

## 📋 İçindekiler
1. [Mimarisi ve Teknolojiler](#1-mimari-ve-teknolojiler)
2. [Yerel Geliştirme ve Çalıştırma](#2-yerel-geliştirme-ve-çalıştırma)
3. [Render.com Canlıya Alma (Deploy) Rehberi](#3-rendercom-canlıya-alma-deploy-rehberi)
4. [Android İstemci Bağlantı Ayarı](#4-android-istemci-bağlantı-ayarı)
5. [Socket.io Olayları (Event API)](#5-socketio-olayları-event-api)
6. [Oyun Kuralları & Hamle Matrisi](#6-oyun-kuralları--hamle-matrisi)
7. [Sıkça Karşılaşılan Sorunlar & İpuçları](#7-sıkça-karşılaşılan-sorunlar--ipuçları)

---

## 1. Mimari ve Teknolojiler

Sunucu, oyuncuların eşleşmesini (matchmaking), özel oda kurmasını/katılmasını (private rooms), hamle doğrulamalarını ve tur sonuçlarını gerçek zamanlı olarak senkronize eder.

- **Çalışma Ortamı:** Node.js (v18+)
- **Dil:** TypeScript (~5.8)
- **Web Çatısı:** Express.js (v4.21)
- **Gerçek Zamanlı İletişim:** Socket.io (v4.8)
- **Çalıştırıcı:** `tsx` (TypeScript Execute)

---

## 2. Yerel Geliştirme ve Çalıştırma

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

## 3. Render.com Canlıya Alma (Deploy) Rehberi

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

## 4. Android İstemci Bağlantı Ayarı

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

## 5. Socket.io Olayları (Event API)

### 📤 İstemciden Sunucuya Gönderilen Olaylar (Client ➔ Server)

| Olay Adı | Veri (Payload) | Açıklama |
| :--- | :--- | :--- |
| `request_player_count` | `{}` | Anlık çevrim içi oyuncu sayısını talep eder. |
| `join_matchmaking` | `{ name: string, skin?: string }` | Rastgele bir rakiple eşleşme sırasına girer. |
| `create_private_room` | `{ name: string, skin?: string }` | 6 haneli özel oda kodu oluşturur ve rakip bekler. |
| `join_private_room` | `{ roomId: string, name: string, skin?: string }` | Kodunu bildiği özel odaya katılır. |
| `send_move` | `{ move: "rock" \| "paper" \| "scissors" \| "iron" \| "ice" \| "steel" }` | O tur için yapılan hamleyi iletir. |
| `leave_game` | `{}` | Oyuncu maçtan veya odadan bilerek ayrıldığında (terk ettiğinde) sunucuya bildirir. |
| `timeout_from_client` | `{}` | Oyuncunun 10 saniyelik süresi dolduğunda otomatik turu sonlandırmak için çağrılır. |

---

### 📥 Sunucudan İstemciye Gönderilen Olaylar (Server ➔ Client)

| Olay Adı | Veri (Payload) | Açıklama |
| :--- | :--- | :--- |
| `player_count` | `{ count: number }` | Anlık bağlı aktif çevrim içi oyuncu sayısı. |
| `waiting_for_opponent` | `{ roomId: string }` | Eşleşme veya oda açıldıktan sonra rakip bekleniyor bildirimi. |
| `private_room_created` | `{ roomId: string }` | Özel oda başarıyla oluşturuldu, oda kodu üretildi. |
| `join_error` | `{ message: string }` | Oda dolu, bulunamadı veya katılım hatası bildirimi. |
| `match_found` | `{ roomId, round, draws, players: [...] }` | Rakip bulundu; oyuncu isimleri ve skinleri iletildi. |
| `game_starting` | `{}` | 3 saniyelik geri sayım sonrası maç ekranının yüklenmesi talimatı. |
| `opponent_moved` | `{}` | Rakibin hamlesini yaptığını bildirir (hamlenin içeriği tur bitene kadar gizlenir). |
| `round_result` | `{ result: "win" \| "lose" \| "draw", opponentMove, score, opponentScore, draws, round }` | Tur değerlendirme sonucu. |
| `next_round` | `{ round: number }` | Yeni turun başladığını bildirir. |
| `game_over` | `{ result: "win" \| "lose" \| "draw" }` | 10 tur tamamlandığında nihai maç galibini bildirir. |
| `opponent_disconnected` | `{ wasPlaying: boolean, round: number }` | Rakip maç devam ederken oyundan ayrıldı / terk etti. (5 turun üzerindeyse kalan oyuncu galip sayılır). |

---

## 6. Oyun Kuralları & Hamle Matrisi

Sunucu ([server.ts](file:///c:/Onur/Android%20Uygulamalar-Oyunlar/Rock-Paper-Scissors_Game/server/server.ts)) ile Android istemcisi ([GameEngine.kt](file:///c:/Onur/Android%20Uygulamalar-Oyunlar/Rock-Paper-Scissors_Game/app/src/main/java/com/onurkolofficial/spsgame/model/GameEngine.kt)) aynı kural matrisini işletir:

| Hamle | Kazanır (Yener) | Kaybeder (Yenilir) | Berabere |
| :--- | :--- | :--- | :--- |
| **Taş (Rock)** | Makas, Demir | Kağıt, Buz, Çelik | Taş |
| **Kağıt (Paper)** | Taş, Demir, Çelik | Makas, Buz | Kağıt |
| **Makas (Scissors)** | Kağıt | Taş, Demir, Buz, Çelik | Makas |
| **Demir (Iron)** | Taş, Makas | Kağıt, Buz, Çelik | Demir |
| **Buz (Ice)** | Taş, Kağıt, Makas, Demir | Çelik | Buz |
| **Çelik (Steel)** | Demir, Taş, Makas, Buz | Kağıt | Çelik |

---

## 7. Sıkça Karşılaşılan Sorunlar & İpuçları

### 1. Render Free Tier Uyku Modu (Cold Start)
- Render.com'un ücretsiz planında sunucuya **15 dakika boyunca** herhangi bir istek gelmezse sunucu uyku moduna geçer (*spun down*).
- Bir kullanıcı uygulamayı açtığında ilk bağlantının kurulması **30-50 saniye** sürebilir. Sunucu uyandıktan sonra tüm istekler anlık (milisaniyeler içinde) yanıt verir.
- *İpucu:* Sunucuyu sürekli uyanık tutmak isterseniz [UptimeRobot](https://uptimerobot.com/) veya [Cron-Job.org](https://cron-job.org/) gibi ücretsiz servislerle sunucunuzun ana URL'sine (`https://your-service.onrender.com/`) her 10 dakikada bir HTTP GET isteği gönderebilirsiniz.

### 2. Yerel Testte Bağlantı Kurulamaması (Android Cleartext Traffic)
- Android 9 (Pie) ve üzerinde yerel IP'lere (`http://`) yapılan istekler varsayılan olarak engellenebilir. Yerel geliştirme yaparken `AndroidManifest.xml` dosyasında `<application android:usesCleartextTraffic="true" ...>` ayarının açık olduğundan emin olun (Canlı HTTPS/WSS bağlantılarında buna gerek yoktur).
