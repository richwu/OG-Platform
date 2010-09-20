/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;

/**
 * 
 *
 */
public class ScheduleCalculator {
  private static final ZonedDateTime[] EMPTY_ARRAY = new ZonedDateTime[0];

  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final Frequency frequency) {
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(frequency);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    return getUnadjustedDateSchedule(effectiveDate, effectiveDate, maturityDate, frequency);
  }

  public static ZonedDateTime[] getUnadjustedDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime accrualDate, final ZonedDateTime maturityDate, final Frequency frequency) {
    Validate.notNull(effectiveDate);
    Validate.notNull(accrualDate);
    Validate.notNull(maturityDate);
    Validate.notNull(frequency);
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    if (accrualDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Accrual date was after maturity");
    }
    // TODO what if there's no valid date between accrual date and maturity date?
    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    final Period period = periodFrequency.getPeriod();
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = effectiveDate; // TODO this is only correct if effective date = accrual date
    while (date.isBefore(maturityDate)) { // REVIEW: could speed this up by working out how many periods between start and end date?
      dates.add(date);
      date = date.plus(period);
    }
    //dates.remove(0);
    return dates.toArray(EMPTY_ARRAY);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(final ZonedDateTime[] dates, final BusinessDayConvention convention, final Calendar calendar) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      result[i] = convention.adjustDate(calendar, dates[i]);
    }
    return result;
  }
  
  public static ZonedDateTime[] getAdjustedResetDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime[] dates, 
                                                             final BusinessDayConvention convention, final Calendar calendar, 
                                                             final int settlementDays) {
    Validate.notNull(effectiveDate);
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    final int n = dates.length;
    final ZonedDateTime[] result = new ZonedDateTime[n];
    result[0] = effectiveDate;
    for (int i = 1; i < n; i++) {
      result[i] = dates[i - 1].plusDays(settlementDays); 
    }
    return result;
  }
  
  public static ZonedDateTime[] getAdjustedMaturityDateSchedule(final ZonedDateTime effectiveDate, final ZonedDateTime[] dates,
      final BusinessDayConvention convention, final Calendar calendar, final Frequency frequency) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(convention);
    Validate.notNull(calendar);
    Validate.notNull(frequency);
    PeriodFrequency periodFrequency;
    if (frequency instanceof PeriodFrequency) {
      periodFrequency = (PeriodFrequency) frequency;
    } else if (frequency instanceof SimpleFrequency) {
      periodFrequency = ((SimpleFrequency) frequency).toPeriodFrequency();
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }
    final Period period = periodFrequency.getPeriod();
   
    int n = dates.length;
    ZonedDateTime[] results = new ZonedDateTime[n];
    results[0] = effectiveDate.plus(period);
    for (int i = 1; i < n; i++) {
      results[i] = dates[i - 1].plus(period); //TODO need to further shift these dates by a convention 
    }
   
    return results;
    
  }
  

  public static double[] getTimes(final ZonedDateTime[] dates, final DayCount dayCount, final ZonedDateTime effectiveDate) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(dayCount);
    Validate.notNull(effectiveDate);
    final int n = dates.length;
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      result[i] = dayCount.getDayCountFraction(effectiveDate, dates[i]);
    }
    return result;
  }
  
  public static double[] getYearFractions(final ZonedDateTime[] dates, final DayCount dayCount, final ZonedDateTime effectiveDate) {
    Validate.notNull(dates);
    Validate.notEmpty(dates);
    Validate.notNull(dayCount);
    Validate.notNull(effectiveDate);
    final int n = dates.length;
    final double[] result = new double[n];
    result[0] = dayCount.getDayCountFraction(effectiveDate, dates[0]);
    for (int i = 1; i < n; i++) {
      result[i] = dayCount.getDayCountFraction(dates[i - 1], dates[i]);
    }
    return result;
  }
}
