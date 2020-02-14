package net.tiny.feature.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.tiny.config.JsonParser;
import net.tiny.feature.assess.Appraiser;
import net.tiny.feature.assess.Assessment;
import net.tiny.feature.assess.Assets;
import net.tiny.feature.assess.Form;
import net.tiny.ws.cache.BarakbCache;

/**
 * 数值化资历评估WEB-API
 * 参数id为评价策略表id
 * 输出JSON格式评估结果
 */
//API: /api/v1/assess/{id}/scores
@Path("/")
public class AssessmentService {

    private transient Appraiser appraiser = new Appraiser();
    private transient PoliciesCache cache;
    private int cacheSize = -1;

    @POST
    @Path("api/vi/assess/{id}/scores")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Assessment.Scores assess(@PathParam("id") Integer id, @BeanParam Assets.Qualifications qualifications) throws IOException {
        Form.Tables tables = getTables(id);
        return appraiser.assess(tables, qualifications).toScores();
    }

    private Form.Tables getTables(Integer id) throws IOException {
        if (cacheSize > 0 && cache == null) {
            // Cache max files
            cache = new PoliciesCache(cacheSize);
        }
        if (cache != null) {
            return cache.get(id);
        } else {
            return loadTables(id);
        }
    }

    static Form.Tables loadTables(int id) throws IOException {
        URL res = Thread.currentThread().getContextClassLoader().getResource("assess/policies/" + id + ".json");
        InputStream in = res.openStream();
        Form.Tables tables = JsonParser.unmarshal(in, Form.Tables.class);
        in.close();
        return tables;
    }

    static class PoliciesCache {
        private final BarakbCache<Integer, Form.Tables> cache;
        /**
         * Create cache for the last capacity number used file.
         *
         * @param capacity
         */
        public PoliciesCache(int capacity) {
            this.cache = new BarakbCache<>(key -> readTables(1), capacity);
            // Have an error from the file system that a file was deleted.
            this.cache.setRemoveableException(RuntimeException.class);
        }

        public void clear() {
            cache.clear();
        }

        public Form.Tables get(int id) throws IOException {
            try {
                return cache.get(id);
            } catch (Throwable e) {
                Throwable cause = findErrorCause(e);
                if(cause instanceof IOException) {
                    throw (IOException)cause;
                } else {
                    throw new IOException(cause.getMessage(), cause);
                }
            }
        }

        @Override
        public String toString() {
            return cache.toString();
        }

        private Throwable findErrorCause(Throwable err) {
            if(err instanceof IOException)
                return err;
            Throwable cause = err.getCause();
            if (null != cause) {
                return findErrorCause(cause);
            } else {
                return err;
            }
        }

        private Form.Tables readTables(int id) {
            try {
                return loadTables(id);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
