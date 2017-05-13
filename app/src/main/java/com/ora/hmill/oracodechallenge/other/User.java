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

import android.graphics.Bitmap;

public class User {
    public String uid;
    public String url;
    public String name;
    public String email;
    public String nickname;
    public String birthday;
    public String aboutme;
    public Bitmap bitmap;

    public User() {}

    public User(String name, String email, String uid, String url, String nickname, String birthday, String aboutme, Bitmap bitmap) {
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.url = url;
        this.nickname = nickname;
        this.birthday = birthday;
        this.aboutme = aboutme;
    }
}

