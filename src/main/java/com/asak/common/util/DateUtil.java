package com.asak.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private DateUtil() {
  }

  public static LocalDateTime now() {
    return LocalDateTime.now(KST);
  }

  public static String formatDate(LocalDate date) {
    return date.format(DATE_FORMAT);
  }

  public static LocalDate parseDate(String date) {
    return LocalDate.parse(date, DATE_FORMAT);
  }

  public static LocalDateTime startOfDay(LocalDate date) {
    return date.atStartOfDay();
  }

  public static LocalDateTime nextDayStart(LocalDate date) {
    return date.plusDays(1).atStartOfDay();
  }
}