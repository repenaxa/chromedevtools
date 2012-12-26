// Copyright (c) 2009 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.sdk.internal.v8native.value;


import org.chromium.sdk.JsEvaluateContext.EvaluateCallback;
import org.chromium.sdk.JsFunction;
import org.chromium.sdk.JsValue;
import org.chromium.sdk.JsValue.Type;
import org.chromium.sdk.JsObjectProperty;
import org.chromium.sdk.JsVariable;
import org.chromium.sdk.RelayOk;
import org.chromium.sdk.SyncCallback;

/**
 * A generic implementation of the JsVariable interface.
 * TODO(TBR-robot): rename into JsVariableBase
 */
public abstract class JsVariableImpl implements JsVariable {

  /** The lazily constructed value of this variable. */
  private final JsValueBase value;

  /** Variable name. */
  private final Object rawName;

  /**
   * Constructs a variable contained in the given context with the given
   * value mirror.
   *
   * @param valueLoader that owns this variable
   * @param valueData for this variable
   */
  public JsVariableImpl(ValueLoader valueLoader, ValueMirror valueData, Object rawName) {
    this.rawName = rawName;
    this.value = createValue(valueLoader, valueData);
  }

  public static JsValueBase createValue(ValueLoader valueLoader, ValueMirror valueData) {
    Type type = valueData.getType();
    switch (type) {
      case TYPE_FUNCTION:
        return new JsFunctionImpl(valueLoader, valueData);
      case TYPE_ERROR:
      case TYPE_OBJECT:
      case TYPE_DATE:
      case TYPE_REGEXP:
        return new JsObjectBase.Impl(valueLoader, valueData);
      case TYPE_ARRAY:
        return new JsArrayImpl(valueLoader, valueData);
      default:
        return new JsValueBase.Impl(valueData);
    }
  }


  /**
   * @return a [probably compound] JsValue corresponding to this variable.
   *         {@code null} if there was an error lazy-loading the value data.
   */
  @Override
  public JsValueBase getValue() {
    return value;
  }

  @Override
  public String getName() {
    return rawName.toString();
  }

  Object getRawNameAsObject() {
    return this.rawName;
  }

  @Override
  public boolean isMutable() {
    return false; // TODO(apavlov): fix once V8 supports it
  }

  @Override
  public boolean isReadable() {
    // TODO(apavlov): implement once the readability metadata are available
    return true;
  }

  @Override
  public synchronized void setValue(String newValue, SetValueCallback callback) {
    // TODO(apavlov): currently V8 does not support it
    if (!isMutable()) {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public String toString() {
    return new StringBuilder()
        .append("[JsVariable: name=")
        .append(getName())
        .append(",value=")
        .append(getValue())
        .append(']')
        .toString();
  }

  /**
   * A non-abstract class that implements JsVariable.
   */
  public static class Impl extends JsVariableImpl {
    public Impl(ValueLoader valueLoader, ValueMirror valueData, Object rawName) {
      super(valueLoader, valueData, rawName);
    }

    @Override public JsObjectProperty asObjectProperty() {
      return null;
    }
  }

  /**
   * An extension to {@JsVariableImpl} that additional provides {@link JsObjectProperty} interface.
   * TODO: properly support getters, setters etc. once supported by protocol.
   */
  static class Property extends JsVariableImpl implements JsObjectProperty {
    public Property(ValueLoader valueLoader, ValueMirror valueData, Object rawName) {
      super(valueLoader, valueData, rawName);
    }
    @Override public JsObjectProperty asObjectProperty() {
      return this;
    }
    @Override public boolean isWritable() {
      return true;
    }
    @Override public JsValue getGetter() {
      return null;
    }
    @Override public JsFunction getGetterAsFunction() {
      return null;
    }
    @Override public JsValue getSetter() {
      return null;
    }
    @Override public boolean isConfigurable() {
      return true;
    }
    @Override public boolean isEnumerable() {
      return true;
    }
    @Override public RelayOk evaluateGet(EvaluateCallback callback, SyncCallback syncCallback) {
      throw new RuntimeException();
    }
  }
}
