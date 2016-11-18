            script workflow

            <UUID>

            <RULE_LABEL_UUID>: when any user performs "Object write"
            on object "<OBJECT_NAME>" of attribute "<INITIAL_CONTAINER>"
              do
                assign object attribute oattr_of_default_obj() to
                  object attribute "<SIGNER_DEST_FOLDER>"
                delete assignment of object attribute oattr_of_default_obj()"
                  to object attribute "<INITIAL_CONTAINER>


            3 times it did this



            <RULE_LABEL>: Move when user performs write
            when user "<USER_NAME>" performs "Object write"
              on object "<OBJECT_NAME>" of attribute "<SOURCE_CONTAINER>"
              do
                assign object attribute oattr_of_default_obj() to
                  object attribute "<DESTINATION_CONTAINER>"
                delete assignment of object attribute oattr_of_default_obj()
                  to object attribute "<INITIAL_CONTAINER>"



            Finally it ended with this
            ""



            <RULE_LABEL>: Last signer performs object write
            when user "<USER_NAME>" performs "Object write"
              on object "<OBJECT_NAME>" of attribute "<SOURCE_CONTAINER>"
              do
                assign object attribute oattr_of_default_obj() to
                  object attribute "<TERMINAL_FOLDER>"
                delete assignment of object attribute oattr_of_default_obj()");
                  to object attribute \"" + sSignerPrev + " witems\"");

