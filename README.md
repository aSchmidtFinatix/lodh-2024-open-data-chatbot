# LODH 2024 Open Data Chatbot

This project is developed as part of the Leipzig Open Data Hackathon 2024.
One of the goals of the hackathon is to make the data at the [open data portal](https://opendata.leipzig.de/) more accessible.
The particular assignment that was worked on here has the title “Open Data Kataloge erschließen”.


## Project Idea

Over the course of the hackathon, a prototype shall be created to facilitate the interaction with the data hosted at the open data portal.
More generally speaking, the DCAT data format will be used as a basis.
This offers the possibility to utilize the project for all kinds of datasets following the DCAT schema.
The idea to achieve this goal, is to use a combination of the following approaches:

* Pre-process available datasets by having them summarized by an LLM.
  Other fields such as categories/tags may be generated as well.
  (The original data is often only described by a single sentence and therefore lacks an informing summary)
* Create text embeddings for the resulting data and store them to a vector database.
* Use user prompts and perform a similarity search against the preprocessed data.
  The similarity search is done for the embedding of the user’s conversation and the newest prompt.
* The most similar datasets are used as a context to generate the answer to the prompt.
* For the user prompts a prototypical chatbot frontend will be implemented.
