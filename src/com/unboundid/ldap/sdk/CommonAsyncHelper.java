/*
 * Copyright 2011-2020 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2011-2020 Ping Identity Corporation
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



/**
 * This interface defines a set of methods that all async helper classes will
 * implement.
 */
interface CommonAsyncHelper
          extends ResponseAcceptor
{
  /**
   * Retrieves the async request ID created for the associated operation.
   *
   * @return  The async request ID created for the associated operation.
   */
  AsyncRequestID getAsyncRequestID();



  /**
   * Retrieves the connection with which the request is associated.
   *
   * @return  The connection with which the request is associated.
   */
  LDAPConnection getConnection();



  /**
   * Retrieves the time (in nanoseconds) at which the associated helper was
   * created.
   *
   * @return  The time (in nanoseconds) at which the associated helper was
   *          created.
   */
  long getCreateTimeNanos();



  /**
   * Retrieves the operation type for the associated operation.
   *
   * @return  The operation type for the associated operation.
   */
  OperationType getOperationType();
}
