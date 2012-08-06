/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.provider.historicaltimeseries;

import java.util.HashMap;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Result from getting one or more historical time-series.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesProviderGetResult extends DirectBean {

  /**
   * The time-series that were obtained.
   */
  @PropertyDefinition
  private final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> _timeSeries = new HashMap<ExternalIdBundle, LocalDateDoubleTimeSeries>();

  /**
   * Creates an instance.
   */
  public HistoricalTimeSeriesProviderGetResult() {
  }

  /**
   * Creates an instance.
   * 
   * @param result  the map of results, not null
   */
  public HistoricalTimeSeriesProviderGetResult(Map<ExternalIdBundle, LocalDateDoubleTimeSeries> result) {
    setTimeSeries(result);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HistoricalTimeSeriesProviderGetResult}.
   * @return the meta-bean, not null
   */
  public static HistoricalTimeSeriesProviderGetResult.Meta meta() {
    return HistoricalTimeSeriesProviderGetResult.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(HistoricalTimeSeriesProviderGetResult.Meta.INSTANCE);
  }

  @Override
  public HistoricalTimeSeriesProviderGetResult.Meta metaBean() {
    return HistoricalTimeSeriesProviderGetResult.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 779431844:  // timeSeries
        return getTimeSeries();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 779431844:  // timeSeries
        setTimeSeries((Map<ExternalIdBundle, LocalDateDoubleTimeSeries>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalTimeSeriesProviderGetResult other = (HistoricalTimeSeriesProviderGetResult) obj;
      return JodaBeanUtils.equal(getTimeSeries(), other.getTimeSeries());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getTimeSeries());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series that were obtained.
   * @return the value of the property
   */
  public Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getTimeSeries() {
    return _timeSeries;
  }

  /**
   * Sets the time-series that were obtained.
   * @param timeSeries  the new value of the property
   */
  public void setTimeSeries(Map<ExternalIdBundle, LocalDateDoubleTimeSeries> timeSeries) {
    this._timeSeries.clear();
    this._timeSeries.putAll(timeSeries);
  }

  /**
   * Gets the the {@code timeSeries} property.
   * @return the property, not null
   */
  public final Property<Map<ExternalIdBundle, LocalDateDoubleTimeSeries>> timeSeries() {
    return metaBean().timeSeries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HistoricalTimeSeriesProviderGetResult}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code timeSeries} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<ExternalIdBundle, LocalDateDoubleTimeSeries>> _timeSeries = DirectMetaProperty.ofReadWrite(
        this, "timeSeries", HistoricalTimeSeriesProviderGetResult.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "timeSeries");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 779431844:  // timeSeries
          return _timeSeries;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesProviderGetResult> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesProviderGetResult>(new HistoricalTimeSeriesProviderGetResult());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesProviderGetResult> beanType() {
      return HistoricalTimeSeriesProviderGetResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code timeSeries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<ExternalIdBundle, LocalDateDoubleTimeSeries>> timeSeries() {
      return _timeSeries;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}