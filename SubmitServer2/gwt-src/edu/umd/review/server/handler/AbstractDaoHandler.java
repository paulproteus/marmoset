package edu.umd.review.server.handler;

import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Provider;

import edu.umd.review.common.CommonConstants;
import edu.umd.review.server.dao.ReviewDao;

public abstract class AbstractDaoHandler<A extends Action<R>, R extends Result> extends
    AbstractActionHandler<A, R> {
  private final Provider<HttpServletRequest> requestProvider;

  protected AbstractDaoHandler(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  protected final ReviewDao getDao() {
    HttpServletRequest request = requestProvider.get();
    String daoKey = request.getHeader(CommonConstants.DAO_KEY_HEADER);
    Preconditions.checkState(!Strings.isNullOrEmpty(daoKey), "No DAO key in request headers.");
    return (ReviewDao) Preconditions.checkNotNull(request.getSession().getAttribute(daoKey),
                                                  "Invalid DAO key in request headers.");
  }
}
