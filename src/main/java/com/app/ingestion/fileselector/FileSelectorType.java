package com.app.ingestion.fileselector;

/**
 * Created by sgu197 on 9/25/2017.
 */
public enum FileSelectorType {

    FILENAME("filename") , SIMPLE("simple");
    String value;

    FileSelectorType(String value) {this.value = value;}
}
