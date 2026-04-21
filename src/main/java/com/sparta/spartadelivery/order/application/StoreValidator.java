package com.sparta.spartadelivery.order.application;

import java.util.UUID;

public interface StoreValidator {
    // store 이후 대체될 예정입니다.
    void validStore(UUID storeId);
}
