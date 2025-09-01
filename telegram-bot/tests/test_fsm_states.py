import pytest
from bot import BotState

class TestFSMStates:
    """Тесты для FSM состояний бота"""
    
    def test_bot_state_enum_values(self):
        """Тест значений состояний бота"""
        assert BotState.NONE.state == "BotState:NONE"
        assert BotState.ADDING_SUBSCRIPTION_NAME.state == "BotState:ADDING_SUBSCRIPTION_NAME"
        assert BotState.ADDING_SUBSCRIPTION_PRICE.state == "BotState:ADDING_SUBSCRIPTION_PRICE"
        assert BotState.ADDING_SUBSCRIPTION_CURRENCY.state == "BotState:ADDING_SUBSCRIPTION_CURRENCY"
        assert BotState.ADDING_SUBSCRIPTION_CYCLE.state == "BotState:ADDING_SUBSCRIPTION_CYCLE"
        assert BotState.ADDING_SUBSCRIPTION_DATE.state == "BotState:ADDING_SUBSCRIPTION_DATE"
        assert BotState.LOGIN_USERNAME.state == "BotState:LOGIN_USERNAME"
        assert BotState.LOGIN_PASSWORD.state == "BotState:LOGIN_PASSWORD"
    
    def test_bot_state_uniqueness(self):
        """Тест уникальности состояний"""
        states = [
            BotState.NONE,
            BotState.ADDING_SUBSCRIPTION_NAME,
            BotState.ADDING_SUBSCRIPTION_PRICE,
            BotState.ADDING_SUBSCRIPTION_CURRENCY,
            BotState.ADDING_SUBSCRIPTION_CYCLE,
            BotState.ADDING_SUBSCRIPTION_DATE,
            BotState.LOGIN_USERNAME,
            BotState.LOGIN_PASSWORD
        ]
        
        # Проверяем, что все состояния уникальны
        state_values = [state.state for state in states]
        assert len(state_values) == len(set(state_values))
    
    def test_bot_state_inheritance(self):
        """Тест наследования от StatesGroup"""
        from aiogram.fsm.state import StatesGroup, State
        
        # Проверяем, что BotState наследуется от StatesGroup
        assert issubclass(BotState, StatesGroup)
        
        # Проверяем, что все атрибуты являются экземплярами State
        for attr_name in dir(BotState):
            if not attr_name.startswith('_'):
                attr = getattr(BotState, attr_name)
                if not callable(attr):
                    assert isinstance(attr, State)

if __name__ == "__main__":
    pytest.main([__file__, "-v"])