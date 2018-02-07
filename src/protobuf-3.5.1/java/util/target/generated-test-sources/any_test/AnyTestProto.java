// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: com/google/protobuf/any_test.proto

package any_test;

public final class AnyTestProto {
  private AnyTestProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface TestAnyOrBuilder extends
      // @@protoc_insertion_point(interface_extends:any_test.TestAny)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.google.protobuf.Any value = 1;</code>
     */
    boolean hasValue();
    /**
     * <code>.google.protobuf.Any value = 1;</code>
     */
    com.google.protobuf.Any getValue();
    /**
     * <code>.google.protobuf.Any value = 1;</code>
     */
    com.google.protobuf.AnyOrBuilder getValueOrBuilder();
  }
  /**
   * Protobuf type {@code any_test.TestAny}
   */
  public  static final class TestAny extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:any_test.TestAny)
      TestAnyOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use TestAny.newBuilder() to construct.
    private TestAny(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private TestAny() {
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private TestAny(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.protobuf.Any.Builder subBuilder = null;
              if (value_ != null) {
                subBuilder = value_.toBuilder();
              }
              value_ = input.readMessage(com.google.protobuf.Any.parser(), extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(value_);
                value_ = subBuilder.buildPartial();
              }

              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return any_test.AnyTestProto.internal_static_any_test_TestAny_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return any_test.AnyTestProto.internal_static_any_test_TestAny_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              any_test.AnyTestProto.TestAny.class, any_test.AnyTestProto.TestAny.Builder.class);
    }

    public static final int VALUE_FIELD_NUMBER = 1;
    private com.google.protobuf.Any value_;
    /**
     * <code>.google.protobuf.Any value = 1;</code>
     */
    public boolean hasValue() {
      return value_ != null;
    }
    /**
     * <code>.google.protobuf.Any value = 1;</code>
     */
    public com.google.protobuf.Any getValue() {
      return value_ == null ? com.google.protobuf.Any.getDefaultInstance() : value_;
    }
    /**
     * <code>.google.protobuf.Any value = 1;</code>
     */
    public com.google.protobuf.AnyOrBuilder getValueOrBuilder() {
      return getValue();
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (value_ != null) {
        output.writeMessage(1, getValue());
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (value_ != null) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, getValue());
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof any_test.AnyTestProto.TestAny)) {
        return super.equals(obj);
      }
      any_test.AnyTestProto.TestAny other = (any_test.AnyTestProto.TestAny) obj;

      boolean result = true;
      result = result && (hasValue() == other.hasValue());
      if (hasValue()) {
        result = result && getValue()
            .equals(other.getValue());
      }
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasValue()) {
        hash = (37 * hash) + VALUE_FIELD_NUMBER;
        hash = (53 * hash) + getValue().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static any_test.AnyTestProto.TestAny parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static any_test.AnyTestProto.TestAny parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static any_test.AnyTestProto.TestAny parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static any_test.AnyTestProto.TestAny parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(any_test.AnyTestProto.TestAny prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code any_test.TestAny}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:any_test.TestAny)
        any_test.AnyTestProto.TestAnyOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return any_test.AnyTestProto.internal_static_any_test_TestAny_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return any_test.AnyTestProto.internal_static_any_test_TestAny_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                any_test.AnyTestProto.TestAny.class, any_test.AnyTestProto.TestAny.Builder.class);
      }

      // Construct using any_test.AnyTestProto.TestAny.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        if (valueBuilder_ == null) {
          value_ = null;
        } else {
          value_ = null;
          valueBuilder_ = null;
        }
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return any_test.AnyTestProto.internal_static_any_test_TestAny_descriptor;
      }

      public any_test.AnyTestProto.TestAny getDefaultInstanceForType() {
        return any_test.AnyTestProto.TestAny.getDefaultInstance();
      }

      public any_test.AnyTestProto.TestAny build() {
        any_test.AnyTestProto.TestAny result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public any_test.AnyTestProto.TestAny buildPartial() {
        any_test.AnyTestProto.TestAny result = new any_test.AnyTestProto.TestAny(this);
        if (valueBuilder_ == null) {
          result.value_ = value_;
        } else {
          result.value_ = valueBuilder_.build();
        }
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof any_test.AnyTestProto.TestAny) {
          return mergeFrom((any_test.AnyTestProto.TestAny)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(any_test.AnyTestProto.TestAny other) {
        if (other == any_test.AnyTestProto.TestAny.getDefaultInstance()) return this;
        if (other.hasValue()) {
          mergeValue(other.getValue());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        any_test.AnyTestProto.TestAny parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (any_test.AnyTestProto.TestAny) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private com.google.protobuf.Any value_ = null;
      private com.google.protobuf.SingleFieldBuilderV3<
          com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> valueBuilder_;
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public boolean hasValue() {
        return valueBuilder_ != null || value_ != null;
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public com.google.protobuf.Any getValue() {
        if (valueBuilder_ == null) {
          return value_ == null ? com.google.protobuf.Any.getDefaultInstance() : value_;
        } else {
          return valueBuilder_.getMessage();
        }
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public Builder setValue(com.google.protobuf.Any value) {
        if (valueBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          value_ = value;
          onChanged();
        } else {
          valueBuilder_.setMessage(value);
        }

        return this;
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public Builder setValue(
          com.google.protobuf.Any.Builder builderForValue) {
        if (valueBuilder_ == null) {
          value_ = builderForValue.build();
          onChanged();
        } else {
          valueBuilder_.setMessage(builderForValue.build());
        }

        return this;
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public Builder mergeValue(com.google.protobuf.Any value) {
        if (valueBuilder_ == null) {
          if (value_ != null) {
            value_ =
              com.google.protobuf.Any.newBuilder(value_).mergeFrom(value).buildPartial();
          } else {
            value_ = value;
          }
          onChanged();
        } else {
          valueBuilder_.mergeFrom(value);
        }

        return this;
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public Builder clearValue() {
        if (valueBuilder_ == null) {
          value_ = null;
          onChanged();
        } else {
          value_ = null;
          valueBuilder_ = null;
        }

        return this;
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public com.google.protobuf.Any.Builder getValueBuilder() {
        
        onChanged();
        return getValueFieldBuilder().getBuilder();
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      public com.google.protobuf.AnyOrBuilder getValueOrBuilder() {
        if (valueBuilder_ != null) {
          return valueBuilder_.getMessageOrBuilder();
        } else {
          return value_ == null ?
              com.google.protobuf.Any.getDefaultInstance() : value_;
        }
      }
      /**
       * <code>.google.protobuf.Any value = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> 
          getValueFieldBuilder() {
        if (valueBuilder_ == null) {
          valueBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                  getValue(),
                  getParentForChildren(),
                  isClean());
          value_ = null;
        }
        return valueBuilder_;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:any_test.TestAny)
    }

    // @@protoc_insertion_point(class_scope:any_test.TestAny)
    private static final any_test.AnyTestProto.TestAny DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new any_test.AnyTestProto.TestAny();
    }

    public static any_test.AnyTestProto.TestAny getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TestAny>
        PARSER = new com.google.protobuf.AbstractParser<TestAny>() {
      public TestAny parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new TestAny(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<TestAny> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TestAny> getParserForType() {
      return PARSER;
    }

    public any_test.AnyTestProto.TestAny getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_any_test_TestAny_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_any_test_TestAny_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\"com/google/protobuf/any_test.proto\022\010an" +
      "y_test\032\031google/protobuf/any.proto\".\n\007Tes" +
      "tAny\022#\n\005value\030\001 \001(\0132\024.google.protobuf.An" +
      "yB\030\n\010any_testB\014AnyTestProtob\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.AnyProto.getDescriptor(),
        }, assigner);
    internal_static_any_test_TestAny_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_any_test_TestAny_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_any_test_TestAny_descriptor,
        new java.lang.String[] { "Value", });
    com.google.protobuf.AnyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}