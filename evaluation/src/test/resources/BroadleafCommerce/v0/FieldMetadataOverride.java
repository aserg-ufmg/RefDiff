/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.openadmin.dto.override;

import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.common.presentation.client.LookupType;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.UnspecifiedBooleanType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.openadmin.dto.MergedPropertyType;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Jeff Fischer
 */
public class FieldMetadataOverride {

  // fields everyone depends on
  private Boolean excluded;
  private String friendlyName;
  private String securityLevel;
  private Integer order;

  public Boolean getExcluded() {
    return excluded;
  }

  public void setExcluded(Boolean excluded) {
    this.excluded = excluded;
  }

  public String getFriendlyName() {
    return friendlyName;
  }

  public void setFriendlyName(String friendlyName) {
    this.friendlyName = friendlyName;
  }

  public String getSecurityLevel() {
    return securityLevel;
  }

  public void setSecurityLevel(String securityLevel) {
    this.securityLevel = securityLevel;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
  }

  // basic fields
  private SupportedFieldType fieldType;
  private SupportedFieldType secondaryType = SupportedFieldType.INTEGER;
  private Integer length;
  private Boolean required;
  private Boolean unique;
  private Integer scale;
  private Integer precision;
  private String foreignKeyProperty;
  private String foreignKeyClass;
  private String foreignKeyDisplayValueProperty;
  private Boolean foreignKeyCollection;
  private MergedPropertyType mergedPropertyType;
  private String[][] enumerationValues;
  private String enumerationClass;
  protected Boolean isDerived;

  // @AdminPresentation derived fields
  private String name;
  private VisibilityEnum visibility;
  private String group;
  private Boolean isBorderlessGroup;
  private Integer groupOrder;
  protected Integer gridOrder;
  private String tab;
  private Integer tabOrder;
  private Boolean groupCollapsed;
  private SupportedFieldType explicitFieldType;
  private Boolean largeEntry;
  private Boolean prominent;
  private String columnWidth;
  private String broadleafEnumeration;
  private Boolean readOnly;
  private Map<String, Map<String, String>> validationConfigurations;
  private Boolean requiredOverride;
  private String tooltip;
  private String helpText;
  private String hint;
  private String lookupDisplayProperty;
  private Boolean forcePopulateChildProperties;
  private Boolean enableTypeaheadLookup;
  private String optionListEntity;
  private String optionValueFieldName;
  private String optionDisplayFieldName;
  private Boolean optionCanEditValues;
  private Serializable[][] optionFilterValues;
  private String showIfProperty;
  private String ruleIdentifier;
  private Boolean translatable;
  private LookupType lookupType;
  private String defaultValue;

  // @AdminPresentationMapField derived fields
  private Boolean searchable;
  private String mapFieldValueClass;

  // Not a user definable field
  private Boolean toOneLookupCreatedViaAnnotation;

  public Boolean getToOneLookupCreatedViaAnnotation() {
    return toOneLookupCreatedViaAnnotation;
  }

  public void setToOneLookupCreatedViaAnnotation(Boolean toOneLookupCreatedViaAnnotation) {
        this.isDerived = isDerived;
    }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SupportedFieldType getExplicitFieldType() {
        return explicitFieldType;
    }
}