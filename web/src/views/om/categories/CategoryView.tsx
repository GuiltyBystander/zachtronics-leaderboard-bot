/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Link, useParams } from "react-router-dom"
import OmRecordGrid from "../../../components/OmRecordGrid"
import ApiResource from "../../../utils/ApiResource"
import fetchFromApi from "../../../utils/fetchFromApi"
import { useEffect } from "react"
import Category from "../../../model/Category"
import { OmRecord } from "../../../model/om/OmRecord"

export default function CategoryView() {
    const params = useParams()
    const categoryId = params.categoryId

    useEffect(() => {
        fetchFromApi<Category>(`/om/category/${categoryId}`).then((category) => (document.title = `${category.displayName} - Opus Magnum Leaderboards`))
    }, [categoryId])

    return (
        <ApiResource<OmRecord[]>
            url={`/om/category/${categoryId}/records`}
            element={(records) => (
                <OmRecordGrid
                    records={records}
                    getTitle={(record) => (
                        <Link
                            to={`/puzzles/${record.puzzle.id}`}
                            style={{
                                color: "inherit",
                                textDecoration: "none",
                            }}
                        >
                            {record.puzzle.displayName}
                        </Link>
                    )}
                    getScore={(record) => record.smartFormattedScore ?? record.fullFormattedScore ?? "None"}
                />
            )}
        />
    )
}
