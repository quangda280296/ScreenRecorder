package com.screen.recorder.Interface;

public interface IRecordListener {

    void onCaptured();

    void onStartRecording();

    void onRecording(String minutes, String seconds);

    void onPauseRecord();

    void onResumeRecord();

    void onStopRecording();
}