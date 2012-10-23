/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Common interface for a resolver component to produce an object from a unique identifier.
 * 
 * @param <T> the common type of the item produced by the resolution
 */
public interface ObjectResolver<T extends UniqueIdentifiable> {

  /**
   * Resolves the unique identifier into the origin object.
   * 
   * @param uniqueId the unique identifier to resolve, not null
   * @param versionCorrection the version/correction timestamp for any deep resolution of the object, not null
   * @return the resolved object, or null if the identifier could not be resolved
   */
  T resolveObject(UniqueId uniqueId, VersionCorrection versionCorrection);

}
