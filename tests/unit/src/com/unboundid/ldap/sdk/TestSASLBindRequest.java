/*
 * Copyright 2015-2020 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2015-2020 Ping Identity Corporation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk;



import com.unboundid.asn1.ASN1OctetString;



/**
 * This class provides an implementation of a SASL bind request that can be used
 * for testing purposes.
 */
public final class TestSASLBindRequest
       extends SASLBindRequest
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -8363518694580911515L;



  // The SASL credentials for this bind request.
  private final ASN1OctetString saslCredentials;



  /**
   * Creates a new instance of this SASL bind request.  It will not have any
   * SASL credentials.
   *
   * @param  controls  The controls to include in the request.
   */
  public TestSASLBindRequest(final Control... controls)
  {
    this(null, controls);
  }



  /**
   * Creates a new instance of this SASL bind request.  It will not have any
   * SASL credentials.
   *
   * @param  saslCredentials  The SASL credentials to use for the request.  It
   *                          may be {@code null} if no credentials are needed.
   * @param  controls         The controls to include in the request.
   */
  public TestSASLBindRequest(final ASN1OctetString saslCredentials,
                             final Control... controls)
  {
    super(controls);

    this.saslCredentials = saslCredentials;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getSASLMechanismName()
  {
    return "TEST";
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected BindResult process(final LDAPConnection connection, final int depth)
            throws LDAPException
  {
    throw new LDAPException(ResultCode.LOCAL_ERROR,
         "This can't really be used to process a request.");
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public TestSASLBindRequest duplicate()
  {
    return duplicate(getControls());
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public TestSASLBindRequest duplicate(final Control[] controls)
  {
    return new TestSASLBindRequest(saslCredentials, controls);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("TestSASLBindRequest()");
  }
}
