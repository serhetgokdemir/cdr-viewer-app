package com.serhet.cdrviewer.exception;

/**
 * API hatalarında istemciye döndürülecek basit yanıt sınıfı.
 * İçerik:
 *  - status: HTTP durum kodu (404, 400 vb.)
 *  - message: hata mesajı
 *  - timeStamp: hatanın oluştuğu zaman (milis)
 */
public class CdrErrorResponse {

    private int status;
    private String message;
    private long timeStamp;

    // Boş constructor (JSON dönüşümü için gerekli)
    public CdrErrorResponse() { }

    // Tüm alanları alan constructor
    public CdrErrorResponse(int status, String message, long timeStamp) {
        this.status = status;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    // Getter ve Setter'lar
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimeStamp() { return timeStamp; }
    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
}