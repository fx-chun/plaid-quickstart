package com.plaid.quickstart.resources;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.StatementsListRequest;
import com.plaid.client.model.StatementsListResponse;
import com.plaid.client.model.StatementsDownloadRequest;
import com.plaid.quickstart.QuickstartApplication;
import okhttp3.ResponseBody;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import retrofit2.Response;

@Path("/statements")
@Produces(MediaType.APPLICATION_JSON)
public class StatementsResource {
  private final PlaidApi plaidClient;

  public StatementsResource(PlaidApi plaidClient) {
    this.plaidClient = plaidClient;
  }

  @GET
  public StatementsListResponse statementsList() throws IOException {

      StatementsListRequest statementsListRequest = new StatementsListRequest()
        .accessToken(QuickstartApplication.accessToken);

      Response<StatementsListResponse> statementsListResponse = plaidClient
        .statementsList(statementsListRequest)
        .execute();

      StatementsDownloadRequest statementsDownloadRequest = new StatementsDownloadRequest()
        .accessToken(QuickstartApplication.accessToken)
        .statementId(statementsListResponse.body().getAccounts().get(0).getStatements().get(0).getStatementId());
        
      Response<ResponseBody> statementsDownloadResponse = plaidClient
        .statementsDownload(statementsDownloadRequest)
        .execute();

      return statementsListResponse.body();

  }
}
