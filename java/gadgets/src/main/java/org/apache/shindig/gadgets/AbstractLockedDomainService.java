/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.gadgets;

import java.util.Collection;
import java.util.Map;

import org.apache.shindig.config.ContainerConfig;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Provides a default implementation of much of the basic functionality for managing locked domains.
 *
 * @since 2.5.0
 */
public abstract class AbstractLockedDomainService implements LockedDomainService {

  /**
   * Used to observer locked domain required in the config. Doing this instead of having
   * AbstractLockedDomainService implement ConfigObserver so that subclasses don't need to know what
   * AbstractLockedDomainService is observing. In order to add the config observer in the constructor
   * this needed to be broken out to avoid initialization order problems when subclassing and
   * calling super()
   */
  private class LockedDomainObserver implements ContainerConfig.ConfigObserver {
    /*
     * (non-Javadoc)
     *
     * @see org.apache.shindig.config.ContainerConfig.ConfigObserver#containersChanged
     * (org.apache.shindig.config.ContainerConfig, java.util.Collection, java.util.Collection)
     */
    public void containersChanged(ContainerConfig config, Collection<String> changed,
            Collection<String> removed) {
      for (String container : changed) {
        required.put(container, config.getBool(container, LOCKED_DOMAIN_REQUIRED_KEY));
      }
      for (String container : removed) {
        required.remove(container);
      }
    }
  }

  protected static final String LOCKED_DOMAIN_REQUIRED_KEY = "gadgets.uri.iframe.lockedDomainRequired";

  protected static final String LOCKED_DOMAIN_FEATURE = "locked-domain";
  private final boolean enabled;

  protected final Map<String, Boolean> required;
  private boolean lockSecurityTokens = false;

  private LockedDomainObserver ldObserver;

  /**
   * Create a LockedDomainService. This constructor should be called by implementors.
   *
   * @param config
   *          the container config that will be observed
   * @param enabled
   *          true if locked domains are enabled; false otherwise
   */
  protected AbstractLockedDomainService(ContainerConfig config, boolean enabled) {
    this.enabled = enabled;
    this.required = Maps.newHashMap();
    if (enabled) {
      this.ldObserver = new LockedDomainObserver();
      config.addConfigObserver(this.ldObserver, true);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.shindig.gadgets.LockedDomainService#getLockedDomainForGadget
   * (org.apache.shindig.gadgets.Gadget, java.lang.String)
   */
  public abstract String getLockedDomainForGadget(Gadget gadget, String container)
          throws GadgetException;

  /*
   * (non-Javadoc)
   *
   * @see org.apache.shindig.gadgets.LockedDomainService#isEnabled()
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.shindig.gadgets.LockedDomainService#isGadgetValidForHost(java .lang.String,
   * org.apache.shindig.gadgets.Gadget, java.lang.String)
   */
  public abstract boolean isGadgetValidForHost(String host, Gadget gadget, String container);

  /*
   * (non-Javadoc)
   *
   * @see org.apache.shindig.gadgets.LockedDomainService#isHostUsingLockedDomain (java.lang.String)
   */
  public abstract boolean isHostUsingLockedDomain(String host);

  /*
   * (non-Javadoc)
   *
   * @see org.apache.shindig.gadgets.LockedDomainService#isSafeForOpenProxy(java .lang.String)
   */
  public boolean isSafeForOpenProxy(String host) {
    if (isEnabled()) {
      return !isHostUsingLockedDomain(host);
    }
    return true;
  }

  /**
   * Allows a renderer to render all gadgets that require a security token on a locked domain. This
   * is recommended security practice, as it secures the token from other gadgets, but because the
   * "security-token" dependency on "locked-domain" is both implicit (added by GadgetSpec code for
   * OAuth elements) and/or transitive (included by opensocial and opensocial-templates features),
   * turning this behavior by default may take some by surprise. As such, we provide this flag. If
   * false (by default), locked-domain will apply only when the gadget's Requires/Optional sections
   * include it. Otherwise, the transitive dependency tree will be traversed to make this decision.
   *
   * @param lockSecurityTokens
   *          If true, locks domains for all gadgets requiring security-token.
   */
  @Inject(optional = true)
  public void setLockSecurityTokens(
          @Named("shindig.locked-domain.lock-security-tokens") Boolean lockSecurityTokens) {
    this.lockSecurityTokens = lockSecurityTokens;
  }

  /**
   * Returns true iff domain locking is enforced for every gadget by the container
   *
   * @param container
   *          the container configuration, e.g., "default"
   * @return true iff domain locking is enforced by the container
   */
  protected boolean isDomainLockingEnforced(String container) {
    return this.required.get(container);
  }

  /**
   * Override methods for custom behavior Allows you to override locked domain feature requests from
   * a gadget.
   */
  protected boolean isExcludedFromLockedDomain(Gadget gadget, String container) {
    return false;
  }

  /**
   * Returns true iff the gadget is requesting to be on a locked domain. If security token locking
   * has been enabled via {@link #setLockSecurityTokens(Boolean)}, this method will return true if
   * the gadget is explicitly or implicitly requesting locked domains; otherwise, this will return
   * true only if the gadget is explicitly requesting locked domains.
   *
   * @param gadget
   *          the gadget
   * @return true iff the gadget is requesting to be on a locked domain
   */
  protected boolean isGadgetReqestingLocking(Gadget gadget) {
    if (this.lockSecurityTokens) {
      return gadget.getAllFeatures().contains(LOCKED_DOMAIN_FEATURE);
    }
    return gadget.getViewFeatures().keySet().contains(LOCKED_DOMAIN_FEATURE);
  }
}
