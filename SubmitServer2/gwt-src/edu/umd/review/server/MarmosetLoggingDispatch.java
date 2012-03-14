package edu.umd.review.server;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ActionHandlerRegistry;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.server.SimpleDispatch;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.review.MarmosetDaoService;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.cs.submitServer.UserSession;
import edu.umd.cs.submitServer.filters.SubmitServerFilter;
import edu.umd.review.common.CommonConstants;
import edu.umd.review.server.dao.ReviewDao;

/** Custom Dispatch implementation that logs code review errors to the database.
 *
 * @author Ryan W Sims (rwsims@umd.edu)
 */
public class MarmosetLoggingDispatch extends SimpleDispatch {
  private final Provider<HttpServletRequest> requestProvider;
  private final Provider<SubmitServerDatabaseProperties> propsProvider;

  @Inject
  public MarmosetLoggingDispatch(ActionHandlerRegistry handlerRegistry,
                                 Provider<HttpServletRequest> requestProvider,
                                 Provider<SubmitServerDatabaseProperties> propsProvider) {
    super(handlerRegistry);
    this.requestProvider = requestProvider;
    this.propsProvider = propsProvider;
  }

  private ReviewDao getDao() {
    HttpServletRequest request = requestProvider.get();
    String daoKey = request.getHeader(CommonConstants.DAO_KEY_HEADER);
    Preconditions.checkState(!Strings.isNullOrEmpty(daoKey), "No DAO key in request headers.");
    return (ReviewDao) Preconditions.checkNotNull(request.getSession().getAttribute(daoKey),
                                                  "Invalid DAO key in request headers.");
  }

  protected <A extends Action<R>, R extends Result> void failed(A action,
                                                                Throwable thrown,
                                                                ActionHandler<A, R> handler,
                                                                ExecutionContext ctx) {
    HttpServletRequest request = requestProvider.get();
    HttpSession session = request.getSession();
    UserSession userSession = (UserSession) session.getAttribute(SubmitServerConstants.USER_SESSION);
    String requestURI = request.getRequestURI();

    ReviewDao dao = getDao();
    if (!(dao instanceof MarmosetDaoService)) {
      throw new ClassCastException("ReviewDao is not a MarmosetDaoService");
    }
    MarmosetDaoService mDao = (MarmosetDaoService) dao;
    Connection conn = null;
    String kind = thrown.getClass().getSimpleName();
    String msg = "CR " + mDao.getCodeReviewerPK() + " -- " + kind;
    String m = thrown.getMessage();

    if (m != null)
      msg += ":" + m;
    SubmitServerDatabaseProperties submitServerDatabaseProperties = propsProvider.get();
    String userAgent = request.getHeader("User-Agent");
    
    try {
      conn = submitServerDatabaseProperties.getConnection();
      ServerError.insert(conn,
                         ServerError.Kind.EXCEPTION,
                         userSession.getStudentPK(),
                         userSession.getStudentPK(),
                         null,
                         mDao.getProjectPK(),
                         mDao.getSubmissionPK(),
                         null,
                         msg,
                         thrown.getClass().getSimpleName(),
                         "gwtCodeReview",
                         requestURI,
                         request.getQueryString(),
                         SubmitServerFilter.getRemoteHost(request),
                         "", userAgent, thrown);
    } catch (SQLException sqlException) {
      assert true; // ignore
    } finally {
      Queries.close(conn);
    }
  };
}
