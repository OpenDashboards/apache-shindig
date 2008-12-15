/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.shindig.social.opensocial.jpa;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;

import org.apache.shindig.social.opensocial.jpa.api.DbObject;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.MediaItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * Activity model object stored in the "activity" table.
 */
@Entity
@Table(name = "activity")
public class ActivityDb implements Activity, DbObject {

  public static final String FINDBY_ACTIVITY_ID = null;

  public static final String PARAM_USERID = null;

  public static final String PARAM_ACTIVITYID = null;

  public static final String JPQL_FINDBY_ACTIVITIES = null;

  /**
   * The internal object ID used for references to this object. Should be
   * generated by the underlying storage mechanism
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "oid")
  protected long objectId;

  /**
   * An optimistic locking field
   */
  @Version
  @Column(name = "version")
  protected long version;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "app_id", length = 255)
  protected String appId;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "body", length = 255)
  protected String body;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "body_id", length = 255)
  protected String bodyId;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "external_id", length = 255)
  protected String externalId;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "activity_id", length = 255)
  protected String id;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "updated")
  @Temporal(TemporalType.TIMESTAMP)
  protected Date updated;

  /**
   * A list of shared media items associated with this activity, joined by the
   * table "activity_media" such that activity_media.activity_id = activity.oid
   * and activity_media.media_id = media.oid. Media items may be shared amongst
   * many activities or other entities.
   */
  @ManyToMany(targetEntity = MediaItemDb.class, cascade = ALL)
  @JoinTable(name = "activity_media",
      joinColumns = @JoinColumn(name = "activity_id", referencedColumnName = "oid"),
      inverseJoinColumns = @JoinColumn(name = "media_id", referencedColumnName = "oid"))
  protected List<MediaItem> mediaItems;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "posted_time")
  protected Long postedTime;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "priority")
  protected Float priority;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "stream_favicon_url", length = 255)
  protected String streamFaviconUrl;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "stream_source_url", length = 255)
  protected String streamSourceUrl;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "stream_title", length = 255)
  protected String streamTitle;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "stream_url", length = 255)
  protected String streamUrl;

  /**
   * Create map using ActivityTemplateParamsDb such that ActivityTemplateParams
   * are joined on oid -> activity_id and then the name value becomes the key,
   * and the value becomes the value unfortunately JPA wont do
   * Map<String,String> so this is handled in the prePersist and postLoad hook.
   */
  @OneToMany(targetEntity = ActivityTemplateParamsDb.class, mappedBy = "activity", cascade = ALL)
  @MapKey(name = "name")
  protected Map<String, ActivityTemplateParamsDb> templateParamsDb = new ConcurrentHashMap<String, ActivityTemplateParamsDb>();

  /**
   * The transient store for templateParamers loaded by the postLoad hook and
   * persisted by the prePersist hook.
   */
  @Transient
  protected Map<String, String> templateParams;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "title", length = 255)
  protected String title;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "title_id", length = 255)
  protected String titleId;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "url", length = 255)
  protected String url;

  /**
   * model field.
   *
   * @see org.apache.shindig.social.opensocial.model.Activity
   */
  @Basic
  @Column(name = "user_id", length = 255)
  protected String userId;

  public ActivityDb() {
  }

  public ActivityDb(String id, String userId) {
    this.id = id;
    this.userId = userId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getAppId()
   */
  public String getAppId() {
    return appId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setAppId(java.lang.String)
   */
  public void setAppId(String appId) {
    this.appId = appId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getBody()
   */
  public String getBody() {
    return body;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setBody(java.lang.String)
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getBodyId()
   */
  public String getBodyId() {
    return bodyId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setBodyId(java.lang.String)
   */
  public void setBodyId(String bodyId) {
    this.bodyId = bodyId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getExternalId()
   */
  public String getExternalId() {
    return externalId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setExternalId(java.lang.String)
   */
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getId()
   */
  public String getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getUpdated()
   */
  public Date getUpdated() {
    if (updated == null) {
      return null;
    }
    return new Date(updated.getTime());
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setUpdated(java.util.Date)
   */
  public void setUpdated(Date updated) {
    if (updated == null) {
      this.updated = null;
    } else {
      this.updated = new Date(updated.getTime());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getMediaItems()
   */
  public List<MediaItem> getMediaItems() {
    return mediaItems;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setMediaItems(java.util.List)
   */
  public void setMediaItems(List<MediaItem> mediaItems) {
    this.mediaItems = mediaItems;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getPostedTime()
   */
  public Long getPostedTime() {
    return postedTime;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setPostedTime(java.lang.Long)
   */
  public void setPostedTime(Long postedTime) {
    this.postedTime = postedTime;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getPriority()
   */
  public Float getPriority() {
    return priority;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setPriority(java.lang.Float)
   */
  public void setPriority(Float priority) {
    this.priority = priority;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getStreamFaviconUrl()
   */
  public String getStreamFaviconUrl() {
    return streamFaviconUrl;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setStreamFaviconUrl(java.lang.String)
   */
  public void setStreamFaviconUrl(String streamFaviconUrl) {
    this.streamFaviconUrl = streamFaviconUrl;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getStreamSourceUrl()
   */
  public String getStreamSourceUrl() {
    return streamSourceUrl;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setStreamSourceUrl(java.lang.String)
   */
  public void setStreamSourceUrl(String streamSourceUrl) {
    this.streamSourceUrl = streamSourceUrl;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getStreamTitle()
   */
  public String getStreamTitle() {
    return streamTitle;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setStreamTitle(java.lang.String)
   */
  public void setStreamTitle(String streamTitle) {
    this.streamTitle = streamTitle;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getStreamUrl()
   */
  public String getStreamUrl() {
    return streamUrl;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setStreamUrl(java.lang.String)
   */
  public void setStreamUrl(String streamUrl) {
    this.streamUrl = streamUrl;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getTemplateParams()
   */
  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setTemplateParams(java.util.Map)
   */
  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getTitle()
   */
  public String getTitle() {
    return title;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setTitle(java.lang.String)
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getTitleId()
   */
  public String getTitleId() {
    return titleId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setTitleId(java.lang.String)
   */
  public void setTitleId(String titleId) {
    this.titleId = titleId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getUrl()
   */
  public String getUrl() {
    return url;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setUrl(java.lang.String)
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#getUserId()
   */
  public String getUserId() {
    return userId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.model.Activity#setUserId(java.lang.String)
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.shindig.social.opensocial.jpa.api.DbObject#getObjectId()
   */
  public long getObjectId() {
    return objectId;
  }

  /**
   * Hook into the pre persist JPA event to take the transient fields and
   * populate the DB fields prior to persisting the data.
   */
  @PrePersist
  public void populateDbFields() {
    // add new entries
    for (Entry<String, String> e : templateParams.entrySet()) {
      ActivityTemplateParamsDb a = templateParamsDb.get(e.getKey());
      if (a == null) {
        a = new ActivityTemplateParamsDb();
        a.name = e.getKey();
        a.value = e.getValue();
        a.activity = this;
        // a.activities = new ArrayList<Activity>();
        // a.activities.add(this);
        templateParamsDb.put(e.getKey(), a);
      } else {
        a.value = e.getValue();
      }
    }
    // remove old entries
    List<String> toRemove = new ArrayList<String>();
    for (Entry<String, ActivityTemplateParamsDb> e : templateParamsDb
        .entrySet()) {
      if (!templateParams.containsKey(e.getKey())) {
        toRemove.add(e.getKey());
      }
    }
    for (String r : toRemove) {
      templateParamsDb.remove(r);
    }
  }

  /**
   * Hook into the post load event in JPA to take the database fields and load
   * the transient fields prior to making the object available to java.
   */
  @PostLoad
  public void loadTransientFields() {
    templateParams = new ConcurrentHashMap<String, String>();
    for (Entry<String, ActivityTemplateParamsDb> e : templateParamsDb
        .entrySet()) {
      templateParams.put(e.getKey(), e.getValue().value);
    }
  }

}
