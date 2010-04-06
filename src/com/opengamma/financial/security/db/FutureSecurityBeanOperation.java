/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.AgricultureFutureSecurity;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.CommodityFutureSecurity;
import com.opengamma.financial.security.EnergyFutureSecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.FutureSecurity;
import com.opengamma.financial.security.FutureSecurityVisitor;
import com.opengamma.financial.security.IndexFutureSecurity;
import com.opengamma.financial.security.InterestRateFutureSecurity;
import com.opengamma.financial.security.MetalFutureSecurity;
import com.opengamma.financial.security.StockFutureSecurity;
import com.opengamma.id.DomainSpecificIdentifier;

/* package */ class FutureSecurityBeanOperation extends Converters implements BeanOperation<FutureSecurity,FutureSecurityBean> {
  
  public static final FutureSecurityBeanOperation INSTANCE = new FutureSecurityBeanOperation ();
  
  private FutureSecurityBeanOperation () {
  }
  
  private static <T> T getSingleElement (final Collection<T> collection) {
    assert collection.size () == 1;
    return collection.iterator ().next ();
  }
  
  private static <T> Set<T> singleElementSet (final T element) {
    final Set<T> set = new HashSet<T> ();
    set.add (element);
    return set;
  }
  
  @Override
  public FutureSecurity createSecurity (final DomainSpecificIdentifier identifier, final FutureSecurityBean bean) {
    return bean.getFutureType ().accept (new FutureType.Visitor<FutureSecurity> () {

      @Override
      public FutureSecurity visitBondFutureType() {
        final Set<FutureBasketAssociationBean> basketBeans = bean.getBasket ();
        final Set<DomainSpecificIdentifier> basket = new HashSet<DomainSpecificIdentifier> (basketBeans.size ());
        if (basketBeans != null) {
          for (FutureBasketAssociationBean basketBean : basketBeans) {
            basket.add (domainSpecificIdentifierBeanToDomainSpecificIdentifier (basketBean.getDomainSpecificIdentifier ()));
          }
        }
        return new BondFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getBondType ().getName (),
            basket
            );
      }

      @Override
      public FutureSecurity visitCurrencyFutureType() {
        return new FXFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            currencyBeanToCurrency (bean.getCurrency2 ()),
            currencyBeanToCurrency (bean.getCurrency3 ()),
            bean.getUnitNumber ()
            );
      }

      @Override
      public FutureSecurity visitInterestRateFutureType() {
        return new InterestRateFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getCashRateType ().getName ()
            );
      }

      @Override
      public FutureSecurity visitAgricultureFutureType() {
        return new AgricultureFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getCommodityType ().getName (),
            bean.getUnitNumber (),
            (bean.getUnitName () != null) ? bean.getUnitName ().getName () : null
            );
      }

      @Override
      public FutureSecurity visitEnergyFutureType() {
        return new EnergyFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getCommodityType ().getName (),
            bean.getUnitNumber (),
            (bean.getUnitName () != null) ? bean.getUnitName ().getName () : null
            );
      }

      @Override
      public FutureSecurity visitMetalFutureType() {
        return new MetalFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getCommodityType ().getName (),
            bean.getUnitNumber (),
            (bean.getUnitName () != null) ? bean.getUnitName ().getName () : null
            );
      }

      @Override
      public FutureSecurity visitIndexFutureType() {
        return new IndexFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            domainSpecificIdentifierBeanToDomainSpecificIdentifier (getSingleElement (bean.getBasket ()).getDomainSpecificIdentifier ())
            );
      }

      @Override
      public FutureSecurity visitStockFutureType() {
        return new StockFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            domainSpecificIdentifierBeanToDomainSpecificIdentifier (getSingleElement (bean.getBasket ()).getDomainSpecificIdentifier ())
            );
      }

    });
  }

  @Override
  public boolean beanEquals(final FutureSecurityBean bean, final FutureSecurity security) {
    return security.accept (new FutureSecurityVisitor<Boolean> () {
      
      private boolean beanEquals (final FutureSecurity security) {
        return ObjectUtils.equals (bean.getFutureType (), FutureType.identify (security))
            && ObjectUtils.equals (dateToExpiry(bean.getExpiry ()), security.getExpiry ())
            && ObjectUtils.equals (bean.getTradingExchange ().getName (), security.getTradingExchange ())
            && ObjectUtils.equals (bean.getSettlementExchange ().getName (), security.getSettlementExchange ());
      }
      
      private boolean beanEquals (final CommodityFutureSecurity security) {
        return beanEquals ((FutureSecurity)security)
            && ObjectUtils.equals (bean.getCommodityType ().getName (), security.getCommodityType ())
            && ObjectUtils.equals ((bean.getUnitName () != null) ? bean.getUnitName ().getName () : null, security.getUnitName ())
            && ObjectUtils.equals (bean.getUnitNumber (), security.getUnitNumber ());
      }

      @Override
      public Boolean visitAgricultureFutureSecurity(
          AgricultureFutureSecurity security) {
        return beanEquals (security);
      }

      @Override
      public Boolean visitBondFutureSecurity(BondFutureSecurity security) {
        if (!beanEquals (security)) return false;
        if (!ObjectUtils.equals (bean.getBondType ().getName (), security.getBondType ())) return false;
        final Set<DomainSpecificIdentifier> basket = security.getBasket ();
        final Set<FutureBasketAssociationBean> beanBasket = bean.getBasket ();
        if (basket == null) {
          if (beanBasket != null) {
            return false;
          }
        } else if (beanBasket == null) {
          return false;
        } else {
          if (basket.size () != beanBasket.size ()) return false;
          for (FutureBasketAssociationBean basketBean : beanBasket) {
            if (!basket.contains (domainSpecificIdentifierBeanToDomainSpecificIdentifier (basketBean.getDomainSpecificIdentifier ()))) return false;
          }
        }
        return true;
      }

      @Override
      public Boolean visitEnergyFutureSecurity(EnergyFutureSecurity security) {
        return beanEquals (security);
      }

      @Override
      public Boolean visitFXFutureSecurity(FXFutureSecurity security) {
        return beanEquals (security)
            && ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency1 ()), security.getNumerator ())
            && ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency2 ()), security.getDenominator ())
            && ObjectUtils.equals (bean.getUnitNumber (), security.getMultiplicationFactor ());
      }

      @Override
      public Boolean visitInterestRateFutureSecurity(
          InterestRateFutureSecurity security) {
        return beanEquals (security)
            && ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency1 ()), security.getCurrency ())
            && ObjectUtils.equals (bean.getCashRateType ().getName (), security.getCashRateType ());
      }

      @Override
      public Boolean visitMetalFutureSecurity(MetalFutureSecurity security) {
        return beanEquals (security)
            && ObjectUtils.equals (domainSpecificIdentifierBeanToDomainSpecificIdentifier (getSingleElement (bean.getBasket ()).getDomainSpecificIdentifier ()), security.getUnderlyingIdentityKey ());
      }

      @Override
      public Boolean visitIndexFutureSecurity(IndexFutureSecurity security) {
        return beanEquals (security)
            && ObjectUtils.equals (domainSpecificIdentifierBeanToDomainSpecificIdentifier (getSingleElement (bean.getBasket ()).getDomainSpecificIdentifier ()), security.getUnderlyingIdentityKey ());
      }

      @Override
      public Boolean visitStockFutureSecurity(StockFutureSecurity security) {
        return beanEquals (security)
        && ObjectUtils.equals (domainSpecificIdentifierBeanToDomainSpecificIdentifier (getSingleElement (bean.getBasket ()).getDomainSpecificIdentifier ()), security.getUnderlyingIdentityKey ());
      }
      
    });
  }

  @Override
  public FutureSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final FutureSecurity security) {
    return security.accept (new FutureSecurityVisitor<FutureSecurityBean> () {
      
      private FutureSecurityBean createBean (final FutureSecurity security) {
        final FutureSecurityBean bean = new FutureSecurityBean ();
        bean.setFutureType (FutureType.identify (security));
        bean.setExpiry (expiryToDate (security.getExpiry ()));
        bean.setTradingExchange (secMasterSession.getOrCreateExchangeBean (security.getTradingExchange (), null));
        bean.setSettlementExchange (secMasterSession.getOrCreateExchangeBean (security.getSettlementExchange (), null));
        bean.setCurrency1 (secMasterSession.getOrCreateCurrencyBean (security.getCurrency ().getISOCode ()));
        return bean;
      }
      
      private FutureSecurityBean createBean (final CommodityFutureSecurity security) {
        final FutureSecurityBean bean = createBean ((FutureSecurity)security);
        bean.setCommodityType (secMasterSession.getOrCreateCommodityFutureTypeBean (security.getCommodityType ()));
        if (security.getUnitName () != null) {
          bean.setUnitName (secMasterSession.getOrCreateUnitNameBean (security.getUnitName ()));
        }
        if (security.getUnitNumber () != null) {
          bean.setUnitNumber (security.getUnitNumber ());
        }
        return bean;
      }

      @Override
      public FutureSecurityBean visitAgricultureFutureSecurity(
          AgricultureFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        return bean;
      }
      
      private FutureBasketAssociationBean createFutureBasketAssociationBean (final FutureSecurityBean bean, final DomainSpecificIdentifier identifier) {
        return new FutureBasketAssociationBean (bean, new DomainSpecificIdentifierBean (identifier.getDomain ().getDomainName (), identifier.getValue ()));
      }
      
      @Override
      public FutureSecurityBean visitBondFutureSecurity(
          BondFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setBondType (secMasterSession.getOrCreateBondFutureTypeBean (security.getBondType ()));
        final Set<DomainSpecificIdentifier> basket = security.getBasket ();
        final Set<FutureBasketAssociationBean> basketBeans = new HashSet<FutureBasketAssociationBean> (basket.size ());
        for (DomainSpecificIdentifier identifier : basket) {
          basketBeans.add (createFutureBasketAssociationBean (bean, identifier));
        }
        bean.setBasket (basketBeans);
        return bean;
      }

      @Override
      public FutureSecurityBean visitEnergyFutureSecurity(
          EnergyFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        return bean;
      }

      @Override
      public FutureSecurityBean visitFXFutureSecurity(FXFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setCurrency2 (secMasterSession.getOrCreateCurrencyBean (security.getNumerator ().getISOCode ()));
        bean.setCurrency3 (secMasterSession.getOrCreateCurrencyBean (security.getDenominator ().getISOCode ()));
        bean.setUnitNumber (security.getMultiplicationFactor ());
        return bean;
      }

      @Override
      public FutureSecurityBean visitInterestRateFutureSecurity(
          InterestRateFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setCashRateType (secMasterSession.getOrCreateCashRateTypeBean (security.getCashRateType ()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitMetalFutureSecurity(
          MetalFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setBasket (singleElementSet (createFutureBasketAssociationBean (bean, security.getUnderlyingIdentityKey ())));
        return bean;
      }

      @Override
      public FutureSecurityBean visitIndexFutureSecurity(IndexFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setBasket (singleElementSet (createFutureBasketAssociationBean (bean, security.getUnderlyingIdentityKey ())));
        return bean;
      }

      @Override
      public FutureSecurityBean visitStockFutureSecurity(StockFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setBasket (singleElementSet (createFutureBasketAssociationBean (bean, security.getUnderlyingIdentityKey ())));
        return bean;
      }
    });
  }

  @Override
  public Class<? extends FutureSecurityBean> getBeanClass() {
    return FutureSecurityBean.class;
  }

  @Override
  public Class<? extends FutureSecurity> getSecurityClass() {
    return FutureSecurity.class;
  }

  @Override
  public String getSecurityType() {
    return "FUTURE";
  }
  
}