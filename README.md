🏗️ ShopApp Teknik Anatomisi
1. Backend: Modern & Güvenli (Python/Flask)
Projenin "beyni" olan bu kısım, sadece veri saklamakla kalmıyor, aynı zamanda farklı servisleri (SQL ve NoSQL) yönetiyor.

Veritabanı (PostgreSQL): İlişkisel verilerin (Kullanıcı-Ürün-Kategori ilişkileri) sarsılmaz güvenlikle saklandığı ana depo.

Güvenlik (JWT - JSON Web Token): Kullanıcı oturumları access_token ile yönetiliyor. Şifreler veritabanında asla düz metin olarak tutulmuyor.

Mimarisi: Service katmanı kullanılarak kod tekrarı önlenmiş ve iş mantığı (business logic) API uç noktalarından (routes) ayrılmış durumda.

Görsel Yönetimi: Ürün resimleri dinamik olarak sunucuda depolanıyor ve URL olarak Android'e servis ediliyor.

2. Frontend: Akıcı & Reaktif (Android/Kotlin/Compose)
Müşterinin dokunduğu bu katman, tamamen modern Android pratikleriyle (Clean Architecture prensiplerine yakın) inşa edildi.

Jetpack Compose: XML yerine tamamen kodla yazılan, modern ve hızlı arayüz bileşenleri.

Hilt (Dependency Injection): Bağımlılıkların (Repository, API, vb.) yönetimi profesyonel bir şekilde otomatikleştirildi.

State Management (StateFlow): Uygulama içindeki veriler (ürün listesi, sepet durumu) reaktif bir şekilde yönetiliyor; veri değiştiği an ekran saniyeler içinde kendini güncelliyor.

Coil: Resimlerin internetten asenkron olarak indirilip cache'lenmesi (önbelleğe alınması) sağlanıyor.

3. Öne Çıkan "Yıldız" Özellikler
Bu projeyi sıradan bir uygulamadan ayıran ve senin mühendislik farkını ortaya koyan kısımlar:

Elasticsearch Entegrasyonu: Milyonlarca ürün arasında milisaniyeler içinde arama yapabilen, yazım hatalarını (Fuzzy Search) tolere eden profesyonel arama altyapısı.

Arama Optimizasyonu (Debounce): Kullanıcının her harf vuruşunda sunucuyu yormayan, yazım bitince devreye giren akıllı arama algoritması.

Dockerize Altyapı: Elasticsearch gibi ağır servislerin Docker konteynerleri içinde izole bir şekilde çalıştırılması.

Senkronizasyon Mekanizması: SQL veritabanındaki bir değişiklik (ürün silme, ekleme) anında arama motoruna (Elasticsearch) yansıtılıyor.
