package com.plaid.quickstart.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plaid.client.model.ConsumerReportPermissiblePurpose;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestCraOptions;
import com.plaid.client.model.LinkTokenCreateRequestStatements;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.request.PlaidApi;
import com.plaid.quickstart.QuickstartApplication;
import retrofit2.Response;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Path("/create_link_token")
@Produces(MediaType.APPLICATION_JSON)
public class LinkTokenResource {
  private final PlaidApi plaidClient;
  private final List<String> plaidProducts;
  private final List<String> countryCodes;
  private final String redirectUri;
  private final List<Products> correctedPlaidProducts;
  private final List<CountryCode> correctedCountryCodes;

  public LinkTokenResource(PlaidApi plaidClient, List<String> plaidProducts,
    List<String> countryCodes, String redirectUri) {
    this.plaidClient = plaidClient;
    this.plaidProducts = plaidProducts;
    this.countryCodes = countryCodes;
    this.redirectUri = redirectUri;
    this.correctedPlaidProducts = new ArrayList<>();
    this.correctedCountryCodes = new ArrayList<>();
  }

  public static class LinkToken {
    @JsonProperty
    private String linkToken;


    public LinkToken(String linkToken) {
      this.linkToken = linkToken;
    }
  }

    /**
     * @param accessToken Optional. If provided, includes the access_token with the link token create
     *                    request, which invokes <a href="https://plaid.com/docs/link/update-mode/">update mode</a>
     */
  @POST
  public LinkToken getLinkToken(@FormParam("access_token") String accessToken) throws IOException {


    String clientUserId = Long.toString((new Date()).getTime());
    LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
		.clientUserId(clientUserId);

    for (int i = 0; i < this.plaidProducts.size(); i++){
      this.correctedPlaidProducts.add(Products.fromValue(this.plaidProducts.get(i)));
    };

    for (int i = 0; i < this.countryCodes.size(); i++){
      this.correctedCountryCodes.add(CountryCode.fromValue(this.countryCodes.get(i)));
    };


		LinkTokenCreateRequest request = new LinkTokenCreateRequest()
			.user(user)
			.clientName("Quickstart Client")
			.products(this.correctedPlaidProducts)
			.countryCodes(this.correctedCountryCodes)
			.language("en")
      .redirectUri(this.redirectUri);

        // If access token was included in the frontend request, include it in our request to Plaid
        //  to invoke update mode
        if (accessToken != null) {
            request.accessToken(accessToken);
        }

    if (this.correctedPlaidProducts.contains(Products.STATEMENTS)) {
      LinkTokenCreateRequestStatements statementsConfig = new LinkTokenCreateRequestStatements()
        .startDate(LocalDate.now().minusDays(30))
        .endDate(LocalDate.now());
      request.setStatements(statementsConfig);
    }

    if (plaidProducts.stream().anyMatch(product -> product.startsWith("cra_"))) {
      request.userToken(QuickstartApplication.userToken);
      request.consumerReportPermissiblePurpose(ConsumerReportPermissiblePurpose.ACCOUNT_REVIEW_CREDIT);
      LinkTokenCreateRequestCraOptions options = new LinkTokenCreateRequestCraOptions();
      options.daysRequested(60);
      request.craOptions(options);
    }

    	Response<LinkTokenCreateResponse> response =plaidClient
			.linkTokenCreate(request)
			.execute();
    return new LinkToken(response.body().getLinkToken());
  }
}
