/*******************************************************************************
 * Copyright (c) 2008, 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial API and implementation
 ******************************************************************************/
package com.eclipsesource.json;

import java.io.IOException;

import org.junit.Test;

import static com.eclipsesource.json.TestUtil.assertException;
import static org.junit.Assert.*;


public class JsonValue_Test {

  @Test
  public void valueOf_int() {
    assertEquals( "0", JsonValue.valueOf( 0 ).toString() );
    assertEquals( "23", JsonValue.valueOf( 23 ).toString() );
    assertEquals( "-1", JsonValue.valueOf( -1 ).toString() );
    assertEquals( "2147483647", JsonValue.valueOf( Integer.MAX_VALUE ).toString() );
    assertEquals( "-2147483648", JsonValue.valueOf( Integer.MIN_VALUE ).toString() );
  }

  @Test
  public void valueOf_long() {
    assertEquals( "0", JsonValue.valueOf( 0l ).toString() );
    assertEquals( "9223372036854775807", JsonValue.valueOf( Long.MAX_VALUE ).toString() );
    assertEquals( "-9223372036854775808", JsonValue.valueOf( Long.MIN_VALUE ).toString() );
  }

  @Test
  public void valueOf_float() {
    assertEquals( "0.0", JsonValue.valueOf( 0f ).toString() );
    assertEquals( "-1.0", JsonValue.valueOf( -1f ).toString() );
    assertEquals( "10.0", JsonValue.valueOf( 10f ).toString() );
    assertEquals( "1.23E-6", JsonValue.valueOf( 0.00000123f ).toString() );
    assertEquals( "-1.23E7", JsonValue.valueOf( -12300000f ).toString() );
  }

  @Test
  public void valueOf_float_failsWithInfinity() {
    String message = "Infinite and NaN values not permitted in JSON";
    assertException( IllegalArgumentException.class, message, new Runnable() {
      public void run() {
        JsonValue.valueOf( Float.POSITIVE_INFINITY );
      }
    } );
  }

  @Test
  public void valueOf_float_failsWithNaN() {
    String message = "Infinite and NaN values not permitted in JSON";
    assertException( IllegalArgumentException.class, message, new Runnable() {
      public void run() {
        JsonValue.valueOf( Float.NaN );
      }
    } );
  }

  @Test
  public void valueOf_double() {
    assertEquals( "10.0", JsonValue.valueOf( 10d ).toString() );
    assertEquals( "1.23E-6", JsonValue.valueOf( 0.00000123d ).toString() );
    assertEquals( "1.7976931348623157E308", JsonValue.valueOf( 1.7976931348623157E308d ).toString() );
  }

  @Test
  public void valueOf_double_failsWithInfinity() {
    String message = "Infinite and NaN values not permitted in JSON";
    assertException( IllegalArgumentException.class, message, new Runnable() {
      public void run() {
        JsonValue.valueOf( Double.POSITIVE_INFINITY );
      }
    } );
  }

  @Test
  public void valueOf_double_failsWithNaN() {
    String message = "Infinite and NaN values not permitted in JSON";
    assertException( IllegalArgumentException.class, message, new Runnable() {
      public void run() {
        JsonValue.valueOf( Double.NaN );
      }
    } );
  }

  @Test
  public void valueOf_boolean() {
    assertSame( JsonValue.TRUE, JsonValue.valueOf( true ) );
    assertSame( JsonValue.FALSE, JsonValue.valueOf( false ) );
  }

  @Test
  public void valueOf_string() {
    assertEquals( "", JsonValue.valueOf( "" ).asString() );
    assertEquals( "Hello", JsonValue.valueOf( "Hello" ).asString() );
    assertEquals( "\"Hello\"", JsonValue.valueOf( "\"Hello\"" ).asString() );
  }

  @Test
  public void valueOf_string_toleratesNull() {
    assertSame( JsonValue.NULL, JsonValue.valueOf( null ) );
  }

  @Test
  public void asObject_failsOnIncompatibleType() {
    assertException( UnsupportedOperationException.class, "Not an object: null", new Runnable() {
      public void run() {
        JsonValue.NULL.asObject();
      }
    } );
  }

  @Test
  public void asArray_failsOnIncompatibleType() {
    assertException( UnsupportedOperationException.class, "Not an array: null", new Runnable() {
      public void run() {
        JsonValue.NULL.asArray();
      }
    } );
  }

  @Test
  public void asString_failsOnIncompatibleType() {
    assertException( UnsupportedOperationException.class, "Not a string: null", new Runnable() {
      public void run() {
        JsonValue.NULL.asString();
      }
    } );
  }

  @Test
  public void asInt_failsOnIncompatibleType() {
    assertException( UnsupportedOperationException.class, "Not a number: null", new Runnable() {
      public void run() {
        JsonValue.NULL.asInt();
      }
    } );
  }

  @Test
  public void asLong_failsOnIncompatibleType() {
    assertException( UnsupportedOperationException.class, "Not a number: null", new Runnable() {
      public void run() {
        JsonValue.NULL.asLong();
      }
    } );
  }

  @Test
  public void asFloat_failsOnIncompatibleType() {
    assertException( UnsupportedOperationException.class, "Not a number: null", new Runnable() {
      public void run() {
        JsonValue.NULL.asFloat();
      }
    } );
  }

  @Test
  public void asDouble_failsOnIncompatibleType() {
    assertException( UnsupportedOperationException.class, "Not a number: null", new Runnable() {
      public void run() {
        JsonValue.NULL.asDouble();
      }
    } );
  }

  @Test
  public void isXxx_returnsFalseForIncompatibleType() {
    JsonValue jsonValue = new JsonValue() {
      @Override
      public void write( JsonWriter jsonWriter ) throws IOException {
      }
    };

    assertFalse( jsonValue.isArray() );
    assertFalse( jsonValue.isObject() );
    assertFalse( jsonValue.isString() );
    assertFalse( jsonValue.isNumber() );
    assertFalse( jsonValue.isBoolean() );
    assertFalse( jsonValue.isNull() );
    assertFalse( jsonValue.isTrue() );
  }

}