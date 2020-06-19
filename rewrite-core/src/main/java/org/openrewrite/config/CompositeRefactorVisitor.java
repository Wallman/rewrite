/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.config;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.openrewrite.Refactor;
import org.openrewrite.SourceFile;
import org.openrewrite.SourceVisitor;
import org.openrewrite.Tree;

import java.util.List;

public class CompositeRefactorVisitor<T extends Tree> extends SourceVisitor<T> {
    private String name;
    private final List<SourceVisitor<T>> delegates;

    public CompositeRefactorVisitor(String name, List<SourceVisitor<T>> delegates) {
        this.name = name;
        this.delegates = delegates;
        delegates.forEach(this::andThen);
    }

    @Override
    public Iterable<Tag> getTags() {
        return Tags.of("name", name);
    }

    CompositeRefactorVisitor<T> setName(String name) {
        this.name = name;
        return this;
    }

    public Class<?> getVisitorType() {
        return delegates.stream().findAny()
                .map(Object::getClass)
                .orElse(null);
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T visitTree(Tree tree) {
        T t = (T) tree;
        if(tree instanceof SourceFile) {
            Refactor<SourceFile, T> refactor = new Refactor<>((SourceFile) t);
            return (T) refactor.visit(delegates).fix().getFixed();
        }

        return super.visitTree(tree);
    }

    @Override
    public T defaultTo(Tree t) {
        return delegates.stream().findAny().map(v -> v.defaultTo(t)).orElse(null);
    }
}
