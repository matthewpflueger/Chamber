--// Add_partner_story_prompts_to_PartnerSettings
-- Migration SQL that makes the change goes here.

alter table PartnerSettings
    add column storyPrompts text null;


--//@UNDO
-- SQL to undo the change goes here.

alter table PartnerSettings
    drop column storyPrompts;


