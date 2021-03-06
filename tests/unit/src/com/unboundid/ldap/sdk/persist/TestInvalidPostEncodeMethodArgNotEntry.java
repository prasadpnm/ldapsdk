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
package com.unboundid.ldap.sdk.persist;



/**
 * This class provides an object with the {@code LDAPObject} annotation that
 * specifies a post-encode method which exists but its argument is not an entry.
 */
@LDAPObject(postEncodeMethod="doPostEncode")
public class TestInvalidPostEncodeMethodArgNotEntry
{
  @LDAPField(attribute="a", inRDN=true, filterUsage=FilterUsage.ALWAYS_ALLOWED)
  private String a;



  /**
   * Performs post-encode processing for this object.
   *
   * @param  arg  The argument for this method.
   */
  private void postEncodeMethod(final String arg)
  {
  }
}
