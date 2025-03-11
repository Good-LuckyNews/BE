package com.draconist.goodluckynews.global.fcm;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface FcmService {

    int sendMessageTo(FcmSendDto fcmSendDto) throws IOException;

}