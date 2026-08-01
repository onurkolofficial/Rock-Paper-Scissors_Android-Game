import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'motion/react';
import { ArrowLeft, ShoppingBag, Coins, Check, Lock as LockIcon, X } from 'lucide-react';
import { Move } from '../model/GameModel';
import { useSettings } from '../contexts/SettingsContext';

export interface Skin {
  id: string;
  nameKey: string;
  emoji: Record<Exclude<Move, 'iron' | 'ice' | 'steel'>, React.ReactNode>;
  cost: number;
}

export const SKINS_LIST: Skin[] = [
  { id: 'default', nameKey: 'skin_default', emoji: { rock: <img src="/gfx_stone.png" alt="Rock" className="w-[1.2em] h-[1.2em] object-contain inline-block align-middle" />, paper: <img src="/gfx_paper.png" alt="Paper" className="w-[1.2em] h-[1.2em] object-contain inline-block align-middle" />, scissors: <img src="/gfx_scissors.png" alt="Scissors" className="w-[1.2em] h-[1.2em] object-contain inline-block align-middle" /> }, cost: 0},
  { id: 'modern', nameKey: 'skin_modern', emoji: { rock: '🪨', paper: '📄', scissors: '✂️' }, cost: 300 },
  { id: 'neon', nameKey: 'skin_neon', emoji: { rock: '💎', paper: '📜', scissors: '⚡' }, cost: 400 },
  { id: 'fancy', nameKey: 'skin_fancy', emoji: { rock: '✊', paper: '✋', scissors: '✌️' }, cost: 500 },
  { id: 'gloved', nameKey: 'skin_gloved', emoji: { rock: '🥊', paper: '🧤', scissors: '✌🏼' }, cost: 750 },
  { id: 'biker', nameKey: 'skin_biker', emoji: { 
    rock: <img src="/gfx_skin_bike_rock.png" className="w-[1.2em] h-[1.2em] object-contain" />, 
    paper: <img src="/gfx_skin_bike_paper.png" className="w-[1.2em] h-[1.2em] object-contain" />, 
    scissors: <img src="/gfx_skin_bike_scissors.png" className="w-[1.2em] h-[1.2em] object-contain" /> 
  }, cost: 1000 },
];

export interface ConsumableItem {
  id: 'iron' | 'ice' | 'steel';
  nameKey: string;
  descKey: string;
  baseCost: number;
  emoji: React.ReactNode;
}

export const CONSUMABLES_LIST: ConsumableItem[] = [
  { id: 'iron', nameKey: 'shop_iron', descKey: 'shop_iron_desc', baseCost: 45, emoji: <img src="/gfx_iron.png" alt="Iron Icon" className="w-full h-full object-contain" /> },
  { id: 'ice', nameKey: 'shop_ice', descKey: 'shop_ice_desc', baseCost: 95, emoji: <span className="text-3xl">🧊</span> },
  { id: 'steel', nameKey: 'shop_steel', descKey: 'shop_steel_desc', baseCost: 100, emoji: <img src="/gfx_steel.svg" alt="Steel Icon" className="w-full h-full object-contain" /> }
];

interface StoreModalProps {
  isOpen: boolean;
  onClose: () => void;
  virtualCash: number;
  ironCount: number;
  iceCount?: number;
  steelCount?: number;
  ownedSkins: string[];
  activeSkinId: string;
  onPurchaseSkin: (skin: Skin) => void;
  onPurchaseItem: (itemId: 'iron' | 'ice' | 'steel', count: number, cost: number) => void;
  onEquipSkin: (skinId: string) => void;
}

const StoreModal: React.FC<StoreModalProps> = ({
  isOpen,
  onClose,
  virtualCash,
  ironCount,
  iceCount = 0,
  steelCount = 0,
  ownedSkins,
  activeSkinId,
  onPurchaseSkin,
  onPurchaseItem,
  onEquipSkin
}) => {
  const { t } = useTranslation();
  const { playSound } = useSettings();
  const [activeShopTab, setActiveShopTab] = useState<'skins' | 'consumables'>('skins');
  const [selectedItem, setSelectedItem] = useState<ConsumableItem | null>(null);
  const [quantity, setQuantity] = useState<number>(5);

  const handleClose = () => {
    playSound('click');
    onClose();
  };

  const totalConsumables = ironCount + iceCount + steelCount;

  const handleBuyItemClick = (item: ConsumableItem) => {
    playSound('click');
    setSelectedItem(item);
    setQuantity(5);
  };

  const confirmPurchase = () => {
    playSound('click');
    if (selectedItem) {
      onPurchaseItem(selectedItem.id, quantity, selectedItem.baseCost * quantity);
      setSelectedItem(null);
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ x: '100%' }}
          animate={{ x: 0 }}
          exit={{ x: '100%' }}
          transition={{ type: 'tween', duration: 0.3, ease: 'easeOut' }}
          className="fixed inset-0 bg-[#0f1112] z-50 flex flex-col p-6 overflow-hidden pt-[env(safe-area-inset-top,24px)]"
        >
          <div className="w-full max-w-2xl mx-auto flex flex-col h-full">
            {/* Header */}
            <div className="flex justify-between items-center mb-6 pt-4 shrink-0">
              <div className="flex items-center gap-3">
                <button 
                  onClick={handleClose}
                  className="p-2 rounded-xl bg-white/5 hover:bg-white/10 active:bg-white/15 border border-white/5 transition flex items-center justify-center cursor-pointer"
                >
                  <ArrowLeft className="w-5 h-5 text-white" />
                </button>
                <div className="flex items-center gap-2">
                  <ShoppingBag className="w-5 h-5 text-yellow-400 animate-pulse" />
                  <h3 className="text-xl font-black tracking-widest uppercase text-white">{t('shop_title')}</h3>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <div className="flex items-center gap-1.5 px-3 py-1 bg-yellow-500/10 border border-yellow-500/20 rounded-full">
                  <Coins className="w-4 h-4 text-yellow-400" />
                  <span className="text-sm font-black text-yellow-300 font-mono">${virtualCash.toLocaleString()}</span>
                </div>
              </div>
            </div>

              {/* Category tabs */}
              <div className="flex border-b border-white/5 mb-6 shrink-0">
                <button 
                  onClick={() => setActiveShopTab('skins')} 
                  className={`flex-1 pb-3 text-sm font-bold uppercase tracking-wider transition ${
                    activeShopTab === 'skins' ? 'text-yellow-400 border-b-2 border-yellow-400' : 'text-white/40'
                  }`}
                >
                  {t('shop_tab_skins')}
                </button>
                <button 
                  onClick={() => setActiveShopTab('consumables')} 
                  className={`flex-1 pb-3 text-sm font-bold uppercase tracking-wider transition relative ${
                    activeShopTab === 'consumables' ? 'text-yellow-400 border-b-2 border-yellow-400' : 'text-white/40'
                  }`}
                >
                  {t('shop_tab_items')}
                  {totalConsumables > 0 && (
                    <span className="ml-1.5 px-2 py-0.5 bg-yellow-400 text-slate-900 font-mono text-[9px] font-black rounded-full">
                      {totalConsumables}
                    </span>
                  )}
                </button>
              </div>

              <div className="flex-1 overflow-y-auto pr-1 space-y-4 min-h-0 pb-6 scrollbar-thin">
                {activeShopTab === 'skins' ? (
                  SKINS_LIST.map((skin) => {
                    const isOwned = ownedSkins.includes(skin.id);
                    const isActive = activeSkinId === skin.id;

                    return (
                      <div 
                        key={skin.id}
                        className={`flex items-center justify-between p-4 rounded-xl border transition-all ${
                          isActive 
                            ? 'bg-yellow-400/5 border-yellow-400/40' 
                            : 'bg-black/20 border-white/5 hover:border-white/10'
                        }`}
                      >
                        <div className="flex items-center gap-4">
                          <div className="w-12 h-12 bg-white/5 rounded-xl border border-white/10 flex items-center justify-center overflow-hidden">
                            <span className="text-3xl select-none leading-none flex items-center justify-center">
                               {skin.emoji.rock}
                            </span>
                          </div>
                          <div className="flex flex-col items-start">
                            <span className="font-bold text-white leading-none">{t(skin.nameKey)}</span>
                            <span className="text-sm text-white tracking-wider flex items-center justify-start gap-1.5 mt-1">
                              <span className="flex items-center justify-center">{skin.emoji.rock}</span>
                              <span className="flex items-center justify-center">{skin.emoji.paper}</span>
                              <span className="flex items-center justify-center">{skin.emoji.scissors}</span>
                            </span>
                          </div>
                        </div>

                        <div>
                          {isActive ? (
                            <div className="flex items-center gap-1.5 py-1.5 px-4 bg-green-500/15 border border-green-500/30 text-green-400 font-bold text-xs rounded-xl uppercase tracking-wider">
                              <Check className="w-4 h-4" /> {t('shop_equipped')}
                            </div>
                          ) : isOwned ? (
                            <button
                              onClick={() => { playSound('click'); onEquipSkin(skin.id); }}
                              className="py-1.5 px-4 bg-white/10 hover:bg-white/15 text-white font-bold text-xs rounded-xl uppercase tracking-wider active:scale-95 transition-all cursor-pointer"
                            >
                              {t('shop_equip')}
                            </button>
                          ) : (
                            <button
                              onClick={() => { playSound('click'); onPurchaseSkin(skin); }}
                              className="flex items-center gap-1 py-1.5 px-4 bg-yellow-400 font-extrabold text-slate-900 text-xs rounded-xl uppercase tracking-wider active:scale-95 transition-all cursor-pointer"
                            >
                              <LockIcon className="w-3.5 h-3.5 mr-0.5" /> {t('shop_buy')} ${skin.cost}
                            </button>
                          )}
                        </div>
                      </div>
                    );
                  })
                ) : (
                  <div className="space-y-3">
                    {CONSUMABLES_LIST.map((item) => (
                      <div 
                        key={item.id}
                        className="flex items-center justify-between p-4 rounded-xl border bg-black/20 border-white/5 hover:border-white/10 transition-all"
                      >
                        <div className="flex items-center gap-4">
                          <div className="w-12 h-12 bg-white/5 rounded-xl border border-white/10 flex items-center justify-center p-2 bg-black/40">
                            {item.emoji}
                          </div>
                          <div className="flex flex-col">
                            <span className="font-bold text-white text-sm">{t(item.nameKey) || item.nameKey}</span>
                            <span className="text-[10px] text-white/40 tracking-wider">
                              {t(item.descKey) || item.descKey}
                            </span>
                          </div>
                        </div>

                        <div className="flex flex-col items-end gap-1">
                          <span className="text-white/50 text-[10px] font-mono px-2 py-0.5 bg-black/40 rounded-md">
                            x{item.id === 'iron' ? ironCount : item.id === 'ice' ? iceCount : steelCount}
                          </span>
                          <button
                            onClick={() => handleBuyItemClick(item)}
                            className="flex items-center gap-1 py-1.5 px-4 bg-yellow-400 font-extrabold text-slate-900 text-[10px] rounded-xl uppercase tracking-wider active:scale-95 transition-all cursor-pointer"
                          >
                            {t('shop_buy')}
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Quantity Selection Modal */}
            <AnimatePresence>
              {selectedItem && (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className="fixed inset-0 bg-black/80 z-[60] flex items-center justify-center p-6 backdrop-blur-sm"
                >
                  <motion.div
                    initial={{ scale: 0.9, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    exit={{ scale: 0.9, opacity: 0 }}
                    className="bg-[#1a1c1e] border border-white/10 p-6 rounded-3xl max-w-sm w-full shadow-2xl relative"
                  >
                    <button 
                      onClick={() => setSelectedItem(null)}
                      className="absolute top-4 right-4 p-2 text-white/40 hover:text-white bg-white/5 rounded-full"
                    >
                      <X className="w-4 h-4" />
                    </button>
                    
                    <div className="text-center mb-6">
                      <div className="w-16 h-16 bg-white/5 rounded-2xl mx-auto flex items-center justify-center p-3 mb-4">
                        {selectedItem.emoji}
                      </div>
                      <h3 className="text-xl font-bold text-white uppercase tracking-wider">{t(selectedItem.nameKey) || selectedItem.nameKey}</h3>
                    </div>

                    <div className="space-y-3 mb-6">
                      {[5, 10, 15].map((amt) => (
                        <button
                          key={amt}
                          onClick={() => { playSound('click'); setQuantity(amt); }}
                          className={`w-full p-4 rounded-xl border flex items-center justify-between transition-all ${
                            quantity === amt 
                              ? 'bg-yellow-400/10 border-yellow-400' 
                              : 'bg-black/20 border-white/5 hover:border-white/20'
                          }`}
                        >
                          <span className={`font-bold ${quantity === amt ? 'text-yellow-400' : 'text-white'}`}>
                            {amt} {t('shop_pieces') || 'Adet'}
                          </span>
                          <span className={`font-mono font-black ${quantity === amt ? 'text-yellow-400' : 'text-white/60'}`}>
                            ${amt * selectedItem.baseCost}
                          </span>
                        </button>
                      ))}
                    </div>

                    <button
                      onClick={confirmPurchase}
                      className="w-full py-4 bg-yellow-400 text-slate-900 font-black rounded-xl uppercase tracking-widest active:scale-95 transition-all shadow-lg shadow-yellow-400/20"
                    >
                      {t('shop_confirm_purchase') || 'SATIN AL'} (${quantity * selectedItem.baseCost})
                    </button>
                  </motion.div>
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
      )}
    </AnimatePresence>
  );
};

export default StoreModal;
