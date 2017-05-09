/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ora.hmill.oracodechallenge.other;

public class ChatMessage {
    public String sender;
    public String receiver;
    public String senderUid;
    public String receiverUid;
    public String senderEmail;
    public String receiverEmail;
    public String message;
    public String timestamp;

    public ChatMessage() {}

    public ChatMessage(String sender, String receiver, String senderUid, String receiverUid, String senderEmail, String receiverEmail, String message, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.message = message;
        this.timestamp = timestamp;
    }
}

