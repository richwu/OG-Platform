/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author elaine
 */
public abstract class ExchangeTradedOptionSecurity extends OptionSecurity {
  private final String _exchange;

  public ExchangeTradedOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final DomainSpecificIdentifier underlyingIdentityKey,
      final Currency currency, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency);
    _exchange = exchange;
  }

  public String getExchange() {
    return _exchange;
  }
  
  public abstract <T> T accept (ExchangeTradedOptionSecurityVisitor<T> visitor);
  
  @Override
  public final <T> T accept (OptionSecurityVisitor<T> visitor) {
    return accept ((ExchangeTradedOptionSecurityVisitor<T>)visitor);
  }
  
}
