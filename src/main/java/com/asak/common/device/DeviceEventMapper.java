package com.asak.common.device;

import org.apache.ibatis.annotations.Mapper;

/**
 * device_event DDL 확정 후 insert/find/claim/finish SQL을 추가한다. 현재는 RTOS API 흐름을 먼저 검증하는 메모리 큐 골격이다.
 */
@Mapper
public interface DeviceEventMapper {}
