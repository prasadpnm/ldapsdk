/*
 * Copyright 2019-2020 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2019-2020 Ping Identity Corporation
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



import java.util.ArrayList;
import java.util.List;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * This class provides an implementation of the SCRAM-SHA-512 SASL mechanism,
 * which is an SCRAM mechanism that uses a SHA-512 digest algorithm and an
 * HmacSHA512 MAC algorithm.
 */
@ThreadSafety(level=ThreadSafetyLevel.NOT_THREADSAFE)
public final class SCRAMSHA512BindRequest
       extends SCRAMBindRequest
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -4396660110665214258L;



  /**
   * Creates a new SCRAM-SHA-512 bind request with the provided information.
   *
   * @param username The username for this bind request.  It must not be {@code
   *                 null} or empty.
   * @param password The password for this bind request.  It must not be {@code
   *                 null} or empty.
   * @param controls The set of controls to include in the bind request.  It may
   *                 be {@code null} or empty if no controls are needed.
   */
  public SCRAMSHA512BindRequest(final String username, final String password,
                                final Control... controls)
  {
    super(username, new ASN1OctetString(password), controls);
  }



  /**
   * Creates a new SCRAM-SHA-512 bind request with the provided information.
   *
   * @param username The username for this bind request.  It must not be {@code
   *                 null} or empty.
   * @param password The password for this bind request.  It must not be {@code
   *                 null} or empty.
   * @param controls The set of controls to include in the bind request.  It may
   *                 be {@code null} or empty if no controls are needed.
   */
  public SCRAMSHA512BindRequest(final String username, final byte[] password,
                                final Control... controls)
  {
    super(username, new ASN1OctetString(password), controls);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getSASLMechanismName()
  {
    return "SCRAM-SHA-512";
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected String getDigestAlgorithmName()
  {
    return "SHA-512";
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected String getMACAlgorithmName()
  {
    return "HmacSHA512";
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public SCRAMSHA512BindRequest getRebindRequest(final String host,
                                                 final int port)
  {
    return duplicate();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public SCRAMSHA512BindRequest duplicate()
  {
    return duplicate(getControls());
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public SCRAMSHA512BindRequest duplicate(final Control[] controls)
  {
    return new SCRAMSHA512BindRequest(getUsername(), getPasswordBytes(),
         controls);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("SCRAMSHA512BindRequest(username='");
    buffer.append(getUsername());
    buffer.append('\'');

    final Control[] controls = getControls();
    if (controls.length > 0)
    {
      buffer.append(", controls={");
      for (int i=0; i < controls.length; i++)
      {
        if (i > 0)
        {
          buffer.append(", ");
        }

        buffer.append(controls[i]);
      }
      buffer.append('}');
    }

    buffer.append(')');
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toCode(final List<String> lineList, final String requestID,
                     final int indentSpaces, final boolean includeProcessing)
  {
    // Create the request variable.
    final List<ToCodeArgHelper> constructorArgs = new ArrayList<>(4);
    constructorArgs.add(ToCodeArgHelper.createString(getUsername(),
         "Username"));
    constructorArgs.add(ToCodeArgHelper.createString("---redacted-password---",
         "Password"));

    final Control[] controls = getControls();
    if (controls.length > 0)
    {
      constructorArgs.add(ToCodeArgHelper.createControlArray(controls,
           "Bind Controls"));
    }

    ToCodeHelper.generateMethodCall(lineList, indentSpaces,
         "SCRAMSHA512BindRequest", requestID + "Request",
         "new SCRAMSHA512BindRequest", constructorArgs);


    // Add lines for processing the request and obtaining the result.
    if (includeProcessing)
    {
      // Generate a string with the appropriate indent.
      final StringBuilder buffer = new StringBuilder();
      for (int i=0; i < indentSpaces; i++)
      {
        buffer.append(' ');
      }
      final String indent = buffer.toString();

      lineList.add("");
      lineList.add(indent + "try");
      lineList.add(indent + '{');
      lineList.add(indent + "  BindResult " + requestID +
           "Result = connection.bind(" + requestID + "Request);");
      lineList.add(indent + "  // The bind was processed successfully.");
      lineList.add(indent + '}');
      lineList.add(indent + "catch (LDAPException e)");
      lineList.add(indent + '{');
      lineList.add(indent + "  // The bind failed.  Maybe the following will " +
           "help explain why.");
      lineList.add(indent + "  // Note that the connection is now likely in " +
           "an unauthenticated state.");
      lineList.add(indent + "  ResultCode resultCode = e.getResultCode();");
      lineList.add(indent + "  String message = e.getMessage();");
      lineList.add(indent + "  String matchedDN = e.getMatchedDN();");
      lineList.add(indent + "  String[] referralURLs = e.getReferralURLs();");
      lineList.add(indent + "  Control[] responseControls = " +
           "e.getResponseControls();");
      lineList.add(indent + '}');
    }
  }
}
