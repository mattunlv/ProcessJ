/*!
 * \brief Class that represents a terminal window
 *
 * A basic class that represents a terminal window.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_SYSTEM_WINDOW_COMPONENT_GROUP_HPP
#define UNLV_PROCESS_J_SYSTEM_WINDOW_COMPONENT_GROUP_HPP

namespace ProcessJSystem{ class WindowComponentGroup; }

class ProcessJSystem::WindowComponentGroup : public ProcessJSystem::WindowComponent, ProcessJSystem::WindowComponent::Listener {

    /// ------------------
    /// Protected Members

protected:

    ProcessJSystem::Vector<ProcessJSystem::WindowComponent*> children; /*< The Children Maintained by the ProcessJSystem::WindowComponentGroup */

    /// --------------
    /// Public Members

public:

    /*!
     * Primary Constructor, Initializes the WindowComponentGroup
     * to its' default state
     *
     * \param listener The ProcessJSystem::WindowComponentGroupListener to receive
     * callbacks on ProcessJRuntim::WindowComponentGroup state mutations
     */

    WindowComponentGroup(ProcessJSystem::WindowComponent::Listener*);

    /*!
     * Default Constructor. Releases any memory managed by
     * the ProcessJSystem::WindowComponentGroup
     */

    virtual ~WindowComponentGroup() { /* Empty */ }

    /// ----------------------------------------
    /// ProcessJSystem::WindowComponentListener

    /*!
     * Invoked when the window component is dirty and needs to be drawn.
     *
     * \param component The Component to be drawn as a void pointer
     */

    void OnComponentDirty(ProcessJSystem::WindowComponent*);

    /*!
     * Invoked when a child view is requesting to be re-measured
     *
     * \parm component The Component that is requesting to be re-measured
     */

   void RequestLayout(ProcessJSystem::WindowComponent*);

   /*!
    * Invoked when a child is releasing itself.
    *
    * \param component The Component that had its' destructor called.
    */

   void OnChildReleased(ProcessJSystem::WindowComponent*);

    /// -------
    /// Methods

    /*!
     * Adds a child ProcessJSystem::WindowComponent to the
     * ProcessJSystem::WindowComponentGroup.
     *
     * \param child The child to add to the WindowComponent tree
     */

    void addChild(ProcessJSystem::WindowComponent&);

};

#endif
