/*
 * Copyright 2009-2020 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2009-2020 Ping Identity Corporation
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
package com.unboundid.ldap.sdk.migrate.ldapjdk;



import java.util.Arrays;

import org.testng.annotations.Test;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPSDKTestCase;



/**
 * This class provides a set of test cases for the {@code LDAPControl} class.
 */
public class LDAPControlTestCase
       extends LDAPSDKTestCase
{
  /**
   * Tests the behavior when creating an LDAP control from an SDK control with
   * no value.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testCreateFromSDKControlWithNoValue()
         throws Exception
  {
    LDAPControl c = new LDAPControl(new Control("1.2.3.4", true));
    c = c.duplicate();

    assertNotNull(c);

    assertNotNull(c.getID());
    assertEquals(c.getID(), "1.2.3.4");

    assertTrue(c.isCritical());

    assertNull(c.getValue());

    assertNotNull(c.toControl());
    assertEquals(c.toControl().getOID(), "1.2.3.4");
    assertEquals(c.toControl().isCritical(), true);
    assertNull(c.toControl().getValue());

    assertNotNull(c.toString());
  }



  /**
   * Tests the behavior when creating an LDAP control from an SDK control with a
   * value.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testCreateFromSDKControlWithValue()
         throws Exception
  {
    LDAPControl c = new LDAPControl(
         new Control("1.2.3.4", false, new ASN1OctetString("foo")));
    c = c.duplicate();

    assertNotNull(c);

    assertNotNull(c.getID());
    assertEquals(c.getID(), "1.2.3.4");

    assertFalse(c.isCritical());

    assertNotNull(c.getValue());
    assertTrue(Arrays.equals(c.getValue(), "foo".getBytes()));

    assertNotNull(c.toControl());
    assertEquals(c.toControl().getOID(), "1.2.3.4");
    assertEquals(c.toControl().isCritical(), false);
    assertNotNull(c.toControl().getValue());

    assertNotNull(c.toString());
  }



  /**
   * Tests the behavior when creating an LDAP control with no value.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testCreateWithNoValue()
         throws Exception
  {
    LDAPControl c = new LDAPControl("1.2.3.4", true, null);
    c = c.duplicate();

    assertNotNull(c);

    assertNotNull(c.getID());
    assertEquals(c.getID(), "1.2.3.4");

    assertTrue(c.isCritical());

    assertNull(c.getValue());

    assertNotNull(c.toControl());
    assertEquals(c.toControl().getOID(), "1.2.3.4");
    assertEquals(c.toControl().isCritical(), true);
    assertNull(c.toControl().getValue());

    assertNotNull(c.toString());
  }



  /**
   * Tests the behavior when creating an LDAP control with a value.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testCreateWithValue()
         throws Exception
  {
    LDAPControl c = new LDAPControl("1.2.3.4", false, "foo".getBytes());
    c = c.duplicate();

    assertNotNull(c);

    assertNotNull(c.getID());
    assertEquals(c.getID(), "1.2.3.4");

    assertFalse(c.isCritical());

    assertNotNull(c.getValue());
    assertTrue(Arrays.equals(c.getValue(), "foo".getBytes()));

    assertNotNull(c.toControl());
    assertEquals(c.toControl().getOID(), "1.2.3.4");
    assertEquals(c.toControl().isCritical(), false);
    assertNotNull(c.toControl().getValue());

    assertNotNull(c.toString());
  }



  /**
   * Provides test coverage for the {@code toControls} method for a null array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToControlsNull()
         throws Exception
  {
    assertNull(LDAPControl.toControls(null));
  }



  /**
   * Provides test coverage for the {@code toControls} method for an empty
   * array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToControlsEmpty()
         throws Exception
  {
    Control[] c = LDAPControl.toControls(new LDAPControl[0]);
    assertNotNull(c);
    assertEquals(c.length, 0);
  }



  /**
   * Provides test coverage for the {@code toControls} method for a
   * single-element array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToControlsSingle()
         throws Exception
  {
    LDAPControl[] lc =
    {
      new LDAPControl("1.2.3.4", true, null)
    };

    Control[] c = LDAPControl.toControls(lc);
    assertNotNull(c);
    assertEquals(c.length, 1);
  }



  /**
   * Provides test coverage for the {@code toControls} method for a
   * multi-element array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToControlsMultiple()
         throws Exception
  {
    LDAPControl[] lc =
    {
      new LDAPControl("1.2.3.4", true, null),
      new LDAPControl("1.2.3.5", false, "foo".getBytes()),
    };

    Control[] c = LDAPControl.toControls(lc);
    assertNotNull(c);
    assertEquals(c.length, 2);
  }



  /**
   * Provides test coverage for the {@code toLDAPControls} method for a null
   * array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToLDAPControlsNull()
         throws Exception
  {
    assertNull(LDAPControl.toLDAPControls(null));
  }



  /**
   * Provides test coverage for the {@code toLDAPControls} method for an empty
   * array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToLDAPControlsEmpty()
         throws Exception
  {
    LDAPControl[] c = LDAPControl.toLDAPControls(new Control[0]);
    assertNotNull(c);
    assertEquals(c.length, 0);
  }



  /**
   * Provides test coverage for the {@code toLDAPControls} method for a
   * single-element array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToLDAPControlsSingle()
         throws Exception
  {
    Control[] c =
    {
      new Control("1.2.3.4", true)
    };

    LDAPControl[] lc = LDAPControl.toLDAPControls(c);
    assertNotNull(lc);
    assertEquals(lc.length, 1);
  }



  /**
   * Provides test coverage for the {@code toLDAPControls} method for a
   * multi-element array.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToLDAPControlsMultiple()
         throws Exception
  {
    Control[] c =
    {
      new Control("1.2.3.4", true),
      new Control("1.2.3.5", false, new ASN1OctetString("foo"))
    };

    LDAPControl[] lc = LDAPControl.toLDAPControls(c);
    assertNotNull(lc);
    assertEquals(lc.length, 2);
  }
}
