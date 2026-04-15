package org.openmrs.module.reportbuilder.api.db.hibernate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.reportbuilder.api.db.LegacyReportDAO;
import org.openmrs.module.reportbuilder.model.LegacyReport;
import org.openmrs.module.reportbuilder.model.LegacyReportConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Hibernate implementation of LegacyReportDAO.
 * Converts between LegacyReport entities and LegacyReportConfig models.
 */
public class HibernateLegacyReportDAO implements LegacyReportDAO {

    private final SessionFactory sessionFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HibernateLegacyReportDAO() {
        this.sessionFactory = null; // Will be injected by Spring
    }

    public HibernateLegacyReportDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<LegacyReportConfig> getAll() {
        Criteria criteria = getCurrentSession().createCriteria(LegacyReport.class);
        criteria.add(Restrictions.eq("retired", false));
        List<LegacyReport> entities = criteria.list();
        return convertToConfigs(entities);
    }

    @Override
    public LegacyReportConfig getByUuid(String uuid) {
        Criteria criteria = getCurrentSession().createCriteria(LegacyReport.class);
        criteria.add(Restrictions.eq("uuid", uuid));
        criteria.add(Restrictions.eq("retired", false));
        LegacyReport entity = (LegacyReport) criteria.uniqueResult();
        return convertToConfig(entity);
    }

    @Override
    public LegacyReportConfig getByName(String name) {
        Criteria criteria = getCurrentSession().createCriteria(LegacyReport.class);
        criteria.add(Restrictions.eq("name", name));
        criteria.add(Restrictions.eq("retired", false));
        LegacyReport entity = (LegacyReport) criteria.uniqueResult();
        return convertToConfig(entity);
    }

    @Override
    public LegacyReportConfig saveOrUpdate(LegacyReportConfig config) {
        LegacyReport entity = convertToEntity(config);
        getCurrentSession().saveOrUpdate(entity);
        return convertToConfig(entity);
    }

    @Override
    public void delete(String uuid) {
        LegacyReport entity = (LegacyReport) getCurrentSession().get(LegacyReport.class, uuid);
        if (entity != null) {
            entity.setRetired(true);
            getCurrentSession().saveOrUpdate(entity);
        }
    }

    @Override
    public List<LegacyReportConfig> getByCategory(String category) {
        Criteria criteria = getCurrentSession().createCriteria(LegacyReport.class);
        criteria.add(Restrictions.eq("category", category));
        criteria.add(Restrictions.eq("retired", false));
        List<LegacyReport> entities = criteria.list();
        return convertToConfigs(entities);
    }

    @Override
    public List<LegacyReportConfig> getByStatus(String status) {
        Criteria criteria = getCurrentSession().createCriteria(LegacyReport.class);
        criteria.add(Restrictions.eq("status", status));
        criteria.add(Restrictions.eq("retired", false));
        List<LegacyReport> entities = criteria.list();
        return convertToConfigs(entities);
    }

    @Override
    public List<LegacyReportConfig> search(String query) {
        Criteria criteria = getCurrentSession().createCriteria(LegacyReport.class);
        criteria.add(Restrictions.or(
            Restrictions.like("name", "%" + query + "%"),
            Restrictions.like("description", "%" + query + "%")
        ));
        criteria.add(Restrictions.eq("retired", false));
        List<LegacyReport> entities = criteria.list();
        return convertToConfigs(entities);
    }

    @Override
    public int getCount() {
        Criteria criteria = getCurrentSession().createCriteria(LegacyReport.class);
        criteria.add(Restrictions.eq("retired", false));
        return criteria.list().size();
    }

    // Conversion methods
    private List<LegacyReportConfig> convertToConfigs(List<LegacyReport> entities) {
        List<LegacyReportConfig> configs = new ArrayList<>();
        for (LegacyReport entity : entities) {
            configs.add(convertToConfig(entity));
        }
        return configs;
    }

    private LegacyReportConfig convertToConfig(LegacyReport entity) {
        if (entity == null) {
            return null;
        }

        try {
            LegacyReportConfig config = objectMapper.readValue(entity.getConfigJson(), LegacyReportConfig.class);
            config.setUuid(entity.getUuid());
            config.setName(entity.getName());
            config.setDescription(entity.getDescription());
            config.setVersion(entity.getVersion());
            config.setCategory(entity.getCategory());
            config.setSubcategory(entity.getSubcategory());
            config.setReportType(entity.getReportType());
            config.setReportYear(entity.getReportYear());
            config.setReportScope(entity.getReportScope());
            config.setStatus(entity.getStatus());

            if (entity.getDateCreated() != null) {
                config.setDateCreated(entity.getDateCreated().toString());
            }
            if (entity.getDateChanged() != null) {
                config.setDateChanged(entity.getDateChanged().toString());
            }

            return config;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert LegacyReport to LegacyReportConfig", e);
        }
    }

    private LegacyReport convertToEntity(LegacyReportConfig config) {
        if (config == null) {
            return null;
        }

        try {
            LegacyReport entity;
            if (config.getUuid() != null) {
                entity = (LegacyReport) getCurrentSession().get(LegacyReport.class, config.getUuid());
                if (entity == null) {
                    entity = new LegacyReport(config.getUuid());
                }
            } else {
                entity = new LegacyReport();
            }

            entity.setName(config.getName());
            entity.setDescription(config.getDescription());
            entity.setVersion(config.getVersion());
            entity.setCategory(config.getCategory());
            entity.setSubcategory(config.getSubcategory());
            entity.setReportType(config.getReportType());
            entity.setReportYear(config.getReportYear());
            entity.setReportScope(config.getReportScope());
            entity.setStatus(config.getStatus());

            String configJson = objectMapper.writeValueAsString(config);
            entity.setConfigJson(configJson);
            entity.setDateChanged(new java.util.Date());

            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert LegacyReportConfig to LegacyReport", e);
        }
    }
}
