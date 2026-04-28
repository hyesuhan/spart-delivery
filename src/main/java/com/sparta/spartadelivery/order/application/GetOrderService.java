package com.sparta.spartadelivery.order.application;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.OrderSearchValidator;
import com.sparta.spartadelivery.order.domain.repository.OrderQueryRepository;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderSearchRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderDetailInfo;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderValidateResult;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrderService {

    private final OrderSearchValidator orderSearchValidator;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderQueryRepository orderQueryRepository;

    public OrderDetailInfo getOrderById(Long userId, UUID orderId) {
        // 본인 or 본인 가게 or manager/master 만 가능하다.
        OrderValidateResult result = orderSearchValidator.validOrderDetails(userId, orderId);

        return OrderDetailInfo.from(result.order(), result.address().getAddress(), result.address().getDetail());
    }

    public Page<OrderSearchResponse> search(Long userId, OrderSearchRequest check, Pageable pageable) {
        orderSearchValidator.validPageParameter(pageable);

        UserEntity user  = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        OrderSearchRequest.SearchCondition condition = switch (user.getRole()) {
            case CUSTOMER -> OrderSearchRequest.SearchCondition.customerCondition(userId, check);
            case OWNER -> {
                Store store = storeRepository.findByOwner(user)
                        .orElseThrow(() -> new AppException(StoreErrorCode.STORE_NOT_FOUND));
                yield OrderSearchRequest.SearchCondition.ownerCondition(userId, check, store.getId());
            }
            case MASTER, MANAGER -> OrderSearchRequest.SearchCondition.adminCondition(userId, check);
        };

        return orderQueryRepository.searchOrders(condition, pageable);
    }


}
