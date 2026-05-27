package com.example.skillora_platform.course.service;

import com.example.skillora_platform.course.dto.BunnyVideoCreated;

public interface BunnyStreamClient {

    BunnyVideoCreated createVideo(String title);
}
