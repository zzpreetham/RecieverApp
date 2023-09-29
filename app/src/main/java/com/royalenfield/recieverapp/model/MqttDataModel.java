package com.royalenfield.recieverapp.model;

public class MqttDataModel
{
    private String id_col ;
    private String raw_data ;
    private String upload_status ;
    private String time_stamp ;

    public MqttDataModel(String id_col, String raw_data, String upload_status, String time_stamp) {
        this.id_col = id_col;
        this.raw_data = raw_data;
        this.upload_status = upload_status;
        this.time_stamp = time_stamp;
    }

    public String getId_col() {
        return id_col;
    }

    public void setId_col(String id_col) {
        this.id_col = id_col;
    }

    public String getRaw_data() {
        return raw_data;
    }

    public void setRaw_data(String raw_data) {
        this.raw_data = raw_data;
    }

    public String getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(String upload_status) {
        this.upload_status = upload_status;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }
}
