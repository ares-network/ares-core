package com.llewkcor.ares.core.factory.data;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

public final class FactoryJob implements MongoDocument<FactoryJob> {
    @Getter public String recipeName;
    @Getter @Setter public long readyTime;

    public FactoryJob() {
        this.recipeName = null;
        this.readyTime = 0;
    }

    /**
     * Creates a new FactoryJob instance with the provided Factory Recipe
     * @param recipe Factory Recipe
     */
    public FactoryJob(FactoryRecipe recipe) {
        this.recipeName = recipe.getName();
        this.readyTime = Time.now() + (recipe.getJobTime() * 1000L);
    }

    /**
     * Creates a new FactoryJob instance with a custom provided ready time
     * @param recipe Recipe
     * @param readyTime Ready Time
     */
    public FactoryJob(FactoryRecipe recipe, int readyTime) {
        this.recipeName = recipe.getName();
        this.readyTime = Time.now() + (readyTime * 1000L);
    }

    /**
     * Returns true if this FactoryJob is complete
     * @return True if compelte
     */
    public boolean isReady() {
        return Time.now() >= readyTime;
    }

    @Override
    public FactoryJob fromDocument(Document document) {
        this.recipeName = document.getString("recipe_name");
        this.readyTime = document.getLong("ready_time");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("recipe_name", recipeName)
                .append("ready_time", readyTime);
    }
}