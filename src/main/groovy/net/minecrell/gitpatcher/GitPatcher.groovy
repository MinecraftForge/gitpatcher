/*
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.minecrell.gitpatcher

import net.minecrell.gitpatcher.task.FindGitTask
import net.minecrell.gitpatcher.task.UpdateSubmodulesTask
import net.minecrell.gitpatcher.task.patch.ApplyPatchesTask
import net.minecrell.gitpatcher.task.patch.MakePatchesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitPatcher implements Plugin<Project> {

    protected Project project
    protected PatchExtension extension

    @Override
    void apply(Project project) {
        this.project = project
        project.with {
            this.extension = extensions.create('patches', PatchExtension)
            extension.root = projectDir

            task('findGit', type: FindGitTask)
            task('updateSubmodules', type: UpdateSubmodulesTask, dependsOn: 'findGit')
            task('applyPatches', type: ApplyPatchesTask/*, dependsOn: 'updateSubmodules' We don't want to update the submodule if we're targeting a specific commit */)
            task('makePatches', type: MakePatchesTask, dependsOn: 'findGit')

            afterEvaluate {
                // Configure the settings from our extension
                tasks.findGit.submodule = extension.submodule

                configure([tasks.applyPatches, tasks.makePatches]) {
                    repo = extension.target
                    root = extension.root
                    submodule = extension.submodule
                    patchDir = extension.patches
                }

                tasks.applyPatches.updateTask = tasks.updateSubmodules

                tasks.updateSubmodules.with {
                    repo = extension.root
                    submodule = extension.submodule
                }
            }
        }
    }

    Project getProject() {
        return project
    }

    PatchExtension getExtension() {
        return extension
    }

}
