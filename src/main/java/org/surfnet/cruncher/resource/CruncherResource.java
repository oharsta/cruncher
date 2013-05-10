/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.surfnet.cruncher.resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginDataInterval;
import org.surfnet.cruncher.model.SpStatistic;
import org.surfnet.cruncher.model.UserStatistics;
import org.surfnet.cruncher.repository.StatisticsRepository;
import org.surfnet.oaaas.auth.AuthorizationServerFilter;
import org.surfnet.oaaas.model.VerifyTokenResponse;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Named
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public class CruncherResource {

  private static final Logger LOG = LoggerFactory.getLogger(CruncherResource.class);

  @Inject
  private StatisticsRepository statisticsRepository;

  @GET
  @Path("/statistics")
  public Response getAllStatisticsForUser(@Context
                         HttpServletRequest request) {
    UserStatistics result = new UserStatistics();
    
    // retrieve IDP and USER information from oauth token
    // retrieve list of 'active SPs'
    final List<SpStatistic> recentLogins = getMockSpStatistics();
    
    // retrieve list of Sps for which the user has given consent
    LOG.debug("returning mocked response for user statistics");
    final List<SpStatistic> consentSps = getMockSpStatistics();

    result.setActiveSps(recentLogins);
    result.setConsentSps(consentSps);
    return Response.ok(result).build();
  }
  
  @GET
  @Path("/uniqueLogins/{startDate}/{endDate}")
  public Response getUniqueLogins(@Context HttpServletRequest request,
      @PathParam("startDate") long startDate,
      @PathParam("endDate") long endDate,
      @QueryParam("idpEntityId") String idpEntityId,
      @QueryParam("spEntityId") String spEntityId) {
    // start and end date are required
    // idp en sp entity id are optional, if neither is given -> error
    if (StringUtils.isBlank(idpEntityId) && StringUtils.isBlank(spEntityId)) {
      throw new IllegalArgumentException("Either idp or sp entity ID is required for this call");
    }
    
    LOG.debug("returning mocked response for unique logins. startDate " + startDate + " endData " + endDate + " idpEntityId " + idpEntityId + " spEntityId " + spEntityId);
    //TODO count unique logins instead of returning them all
    List<LoginData> result = statisticsRepository.getUniqueLogins(new Timestamp(startDate), new Timestamp(endDate), spEntityId, idpEntityId);
    return Response.ok(result).build();
  }
  
  @GET
  @Path("logins/{startDate}/{endDate}")
  public Response getLoginsPerInterval(@Context HttpServletRequest request,
      @PathParam("startDate") long startDate,
      @PathParam("endDate") long endDate,
      @QueryParam("idpEntityId") String idpEntityId,
      @QueryParam("spEntityId") String spEntityId) {
    // start and end date are required
    // idp en sp entity id are optional, if neither is given -> error
    if (StringUtils.isBlank(idpEntityId) && StringUtils.isBlank(spEntityId)) {
      throw new IllegalArgumentException("Either idp or sp entity ID is required for this call");
    }
    
    LOG.debug("returning mocked response for unique logins. startDate " + startDate + " endData " + endDate + " idpEntityId " + idpEntityId + " spEntityId " + spEntityId);
    List<LoginDataInterval> result = getMockedLoginIntervalData();
    return Response.ok(result).build();
  }

  protected String getClientId(HttpServletRequest request) {
    VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getAttribute(AuthorizationServerFilter.VERIFY_TOKEN_RESPONSE);
    return verifyTokenResponse.getPrincipal().getName();
  }

  private List<SpStatistic> getMockSpStatistics() {
    List<SpStatistic> result = new ArrayList<SpStatistic>();
    SpStatistic stat1 = new SpStatistic();
    stat1.setEntryTime(System.currentTimeMillis());
    stat1.setSpEntityId("stats_spEntityId");
    stat1.setSpName("mocked_SP_Name");
    result.add(stat1);
    
    SpStatistic stat2 = new SpStatistic();
    stat2.setEntryTime(System.currentTimeMillis() - (1000L*60L*60L*24L*3L));
    stat2.setSpEntityId("stats_older_spId");
    stat2.setSpName("mocked_Older_SP");
    result.add(stat2);
    
    SpStatistic stat3 = new SpStatistic();
    stat3.setEntryTime(0L);
    stat3.setSpEntityId("stats_oldest_spId");
    stat3.setSpName("mocked_Oldest_SP");
    result.add(stat3);

    return result;
  }

  private void fillLoginDataInterval(LoginData login) {
    login.setIdpEntityId("mocked_idp_id");
    login.setIdpname("mocked_idp");
    login.setSpEntityId("mocked_sp_id");
    login.setSpName("mocked_sp");
    login.setLoginTime(System.currentTimeMillis());
  }
  
  private List<LoginDataInterval> getMockedLoginIntervalData() {
    List<LoginDataInterval> result = new ArrayList<LoginDataInterval>();
    LoginDataInterval loginData =  new LoginDataInterval();
    fillLoginDataInterval(loginData);
    loginData.setPointInterval(1000L*60L*60L*24L);
    result.add(loginData);
    return result;
  }

}
