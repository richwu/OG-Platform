/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.generator.GeneratorSwapTestsMaster;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.TodayPaymentCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * 
 */
public class ConstantSpreadHorizonThetaCalculatorSwapTest {

  // The Calculator
  private static final ConstantSpreadHorizonThetaCalculator CALC = ConstantSpreadHorizonThetaCalculator.getInstance();

  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD Calendar");
  private static final GeneratorSwapTestsMaster GENERATOR_SWAP_MASTER = GeneratorSwapTestsMaster.getInstance();
  private static final IndexIborTestsMaster INDEX_IBOR_MASTER = IndexIborTestsMaster.getInstance();

  // Swap Fixed-Ibor
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR_USD);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);

  // Swap Ibor-ibor
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_MASTER.getIndex("USDLIBOR3M", CALENDAR_USD);
  private static final double SPREAD3 = 0.0020;
  private static final IborIndex USDLIBOR6M = INDEX_IBOR_MASTER.getIndex("USDLIBOR6M", CALENDAR_USD);
  private static final double SPREAD6 = 0.0005;
  private static final SwapIborIborDefinition SWAP_IBORSPREAD_IBORSPREAD_DEFINITION = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL,
      USDLIBOR3M, SPREAD3, true), AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
  private static final SwapDefinition SWAP_IBOR_IBORSPREAD_DEFINITION = new SwapDefinition(AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, true),
      AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));

  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2(USD);
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();
  private static final ParSpreadMarketQuoteCalculator PSC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();

  private static final ArrayZonedDateTimeDoubleTimeSeries FIXING_TS_3 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 11, 15)}, new double[] {
      0.0080, 0.0090, 0.0100, 0.0110, 0.0140, 0.0160});
  private static final ArrayZonedDateTimeDoubleTimeSeries FIXING_TS_6 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16)}, new double[] {0.0095, 0.0120, 0.0130});
  private static final ArrayZonedDateTimeDoubleTimeSeries[] FIXING_TS_3_6 = new ArrayZonedDateTimeDoubleTimeSeries[] {FIXING_TS_3, FIXING_TS_6};
  private static final ConstantSpreadHorizonThetaCalculator THETAC = ConstantSpreadHorizonThetaCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;

  // Tests ///////////////////////////////////////////////////////////////////////

  @Test
  public void thetaFixedIborOverFirstPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
    final double pvToday = PVC.visit(swapToday, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
    final double pvTomorrow = PVC.visit(swapTomorrow, tomorrowData);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();
    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - (pvToday - todayCash), theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentCalculatorOnDayOfPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final ZonedDateTime horizonDate = referenceDate.plusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void todayPaymentCalculatorOnDayBeforePayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 16);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double todayCash = 0.0;

    final ZonedDateTime horizonDate = referenceDate.plusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void todayPaymentCalculatorOnDayAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 18);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double todayCash = 0.0;

    final ZonedDateTime horizonDate = referenceDate.plusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void todayPaymentCalculatorOverWeekIncludingPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 15);
    final ZonedDateTime horizonDate = referenceDate.plusDays(7);

    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(TimeCalculator.getTimeBetween(referenceDate, horizonDate));
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  // BACKWARD LOOKING TESTS //////////////////////////////////////////
  @Test
  public void TpcLookingBackwardOnDayOfPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final ZonedDateTime horizonDate = referenceDate.minusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate); // !!! Negative horizon
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void TpcLookingBackwardOneDayOneDayAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 18);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double todayCash = 0.0; // ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final ZonedDateTime horizonDate = referenceDate.minusDays(1);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate); // !!! Negative horizon
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void TpcLookingBackwardAndForwardOnDayOfPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);

    final ZonedDateTime forwardHorizonDate = referenceDate.minusDays(1);
    final double forwardHorizon = TimeCalculator.getTimeBetween(referenceDate, forwardHorizonDate); // !!! Negative horizon
    final TodayPaymentCalculator forwardCalculator = TodayPaymentCalculator.getInstance(forwardHorizon);
    final MultipleCurrencyAmount paymentIfLookingForward = swapToday.accept(forwardCalculator);

    final ZonedDateTime backwardHorizonDate = referenceDate.minusDays(1);
    final double backwardHorizon = TimeCalculator.getTimeBetween(referenceDate, backwardHorizonDate); // !!! Negative horizon
    final TodayPaymentCalculator backwardCalculator = TodayPaymentCalculator.getInstance(backwardHorizon);
    final MultipleCurrencyAmount paymentIfLookingBackward = swapToday.accept(backwardCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", paymentIfLookingForward.getAmount(USD6MLIBOR3M.getCurrency()), paymentIfLookingBackward.getAmount(USD6MLIBOR3M.getCurrency()),
        TOLERANCE_PV);
  }

  @Test
  // The following test fails because the payment on 2012/08/17 is dropped when toDerivative is called...
  public void TpcLookingBackwardOverWeekOfPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 21);
    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();

    final ZonedDateTime horizonDate = referenceDate.minusDays(7);
    final double horizon = TimeCalculator.getTimeBetween(referenceDate, horizonDate); // !!! Negative horizon
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(horizon);
    final MultipleCurrencyAmount paymentToday = swapToday.accept(paymentCalculator);

    assertEquals("TodayPaymentCalculator: fixed-coupon swap", todayCash, paymentToday.getAmount(USD6MLIBOR3M.getCurrency()), TOLERANCE_PV);
  }
}