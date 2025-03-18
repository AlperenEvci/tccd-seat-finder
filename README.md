# TCDD Seat Finder

TCDD Seat Finder, Türkiye Cumhuriyeti Devlet Demiryolları'nın (TCDD) tren seferleri için boş koltukları bulmanıza yardımcı olan bir uygulamadır. Kullanıcılar, belirli bir tarih aralığında ve belirli istasyonlar arasında boş koltukları sorgulayabilir ve sonuçları e-posta ile alabilir.

## Özellikler

- **Boş Koltuk Sorgulama**: Kullanıcılar, kalkış ve varış istasyonları ile tarih aralığını belirterek boş koltukları sorgulayabilir.
- **E-posta Bildirimi**: Boş koltuklar bulunduğunda, kullanıcıya e-posta ile bildirim gönderilir.
- **Kullanıcı Dostu Arayüz**: RESTful API ile kolay kullanım.

## Teknolojiler

- **Java**: Uygulama geliştirme için.
- **Spring Boot**: RESTful API geliştirmek için.
- **Lombok**: Java sınıflarını daha okunabilir hale getirmek için.
- **JUnit**: Test yazımı için.
- **Maven**: Proje yönetimi ve bağımlılık yönetimi için.

## Kurulum

1. **Proje Klonlama**:

   ```bash
   git clone https://github.com/kullaniciadi/tccd-seat-finder.git
   cd tccd-seat-finder
   ```

2. **Bağımlılıkları Yükleme**:
   Maven kullanarak bağımlılıkları yükleyin:

   ```bash
   mvn install
   ```

3. **Uygulamayı Çalıştırma**:
   Uygulamayı başlatmak için:

   ```bash
   mvn spring-boot:run
   ```

4. **API'yi Test Etme**:
   Postman veya benzeri bir araç kullanarak aşağıdaki endpoint'e istek gönderin:
   ```
   POST http://localhost:8080/available-seats/between-dates/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{startDate}/{endDate}/{seatType}
   ```

## Kullanım

- **Boş Koltuk Sorgulama**:
  - `fromStationId`: Kalkış istasyonu ID'si
  - `fromStationName`: Kalkış istasyonu adı
  - `toStationId`: Varış istasyonu ID'si
  - `toStationName`: Varış istasyonu adı
  - `startDate`: Başlangıç tarihi (örneğin: "18-03-2024 10:00:00")
  - `endDate`: Bitiş tarihi (örneğin: "19-03-2024 23:59:00")
  - `seatType`: Koltuk tipi (örneğin: "EKONOMİ")

## Testler

Projede birim testleri bulunmaktadır. Testleri çalıştırmak için:

```bash
mvn test
```

## Katkıda Bulunma

Katkıda bulunmak isterseniz, lütfen bir pull request oluşturun veya sorunlarınızı bildirin.

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır. Daha fazla bilgi için LICENSE dosyasına bakın.
