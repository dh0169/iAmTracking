package com.iAmTracking.demo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Comparable<Message>{

    @JsonProperty("_id")
    private Integer id;

    @JsonProperty("threadid")
    private Integer threadId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("number")
    private String number;

    @JsonProperty("read")
    private boolean read;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime received;

    @JsonProperty("body")
    private String body;

    public Message() {
        // Default constructor needed for JSON deserialization
    }

    public Message(Integer id, Integer threadId, String type, String number, LocalDateTime received, String body, boolean read) {
        this.id = id;
        this.threadId = threadId;
        this.type = type;
        this.number = number;
        this.received = received;
        this.body = body;
        this.read = read;
    }

    public Message(String number, String body){
        this(-1, -1, "received", number, LocalDateTime.now(), body, false);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getThreadId() {
        return threadId;
    }

    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    public void setReceived(LocalDateTime received) {
        this.received = received;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            ObjectMapper mapper = new ObjectMapper();

            //Need this to parse date or else we get error
            /*
             * Error converting to JSON: Java 8 date/time type `java.time.LocalDateTime` not supported by default:
             * add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable
             * */
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());  // Register the JavaTimeModule
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: disable writing dates as timestamps


            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }

    @Override
    public int compareTo(Message other) {
        return this.id.compareTo(other.id);
    }

}
